package us.jasonrobinson.fun21carnival.data.game

import android.support.annotation.IntRange
import us.jasonrobinson.fun21carnival.data.model.*
import us.jasonrobinson.fun21carnival.data.model.Hand.Action.*
import us.jasonrobinson.fun21carnival.data.util.ChipUtil

abstract class Blackjack(private val shoe: Shoe, dealer: Dealer, val minimumBet: Int) {

    private val seats: List<PlayerSeat>
    private val dealerSeat = DealerSeat(dealer)

    init {
        seats = arrayListOf<PlayerSeat>().apply {
            for (i in 0..7) {
                add(PlayerSeat())
            }
        }
    }

    fun deal() {
        // Shuffle shoe is less than 15% remaining
        if (shoe.remaining() < shoe.size * .15) shuffle()

        val activePlayerSeats = seats.filter { it.isPlaying() }
        val activeSeats = activePlayerSeats.plus(dealerSeat)

        // Deal cards to players and dealer
        activeSeats.forEach {
            it.newHand()
            it.getHand(0).addCard(shoe.takeTop())
        }
        activeSeats.forEach { it.getHand(0).addCard(shoe.takeTop()) }

        // If player Blackjack beats dealer Blackjack, check for player blackjack
        var checkedPlayerBlackjack = false
        if (playerBlackjackBeatsDealerBlackjack()) {
            resolvePlayerBlackjacks(activePlayerSeats)
            checkedPlayerBlackjack = true
        }

        // Check for dealer blackjack
        var dealerBlackjack = false
        val dealerHand = dealerSeat.getHand(0)
        if (dealerHand.getCards().filter { it.second }.map { it.first }.first().rank == Rank.Ace) {
            // Offer insurance
            // NOTE: Insurance can be complicated due to needing to half bets that might result in needing
            // to break down existing chips into smaller chips to make the total. Skipping implementing
            // this for now since insurance is a sucker bet anyway.
//            activePlayerSeats.forEach { if (it.person!!.takeInsurance()) it.betInsurance() }
        }

        if (dealerHand.isBlackjack()) {
            dealerSeat.revealHoleCard()
            payInsurance(activePlayerSeats)
            dealerBlackjack = true
        }

        if (!dealerBlackjack) {
            // Check for player blackjack if we haven't already
            if (!checkedPlayerBlackjack) {
                resolvePlayerBlackjacks(activePlayerSeats)
            }

            // Players play their hands
            activePlayerSeats.forEach {
                val playedHands = arrayListOf<PlayerHand>()
                while (playedHands.size < it.getHands().filter { !it.isResolved() }.size) {
                    for (hand in it.getHands().filter { !it.isResolved() && !playedHands.contains(it) }) {
                        val lastAction = playHand(it, hand)
                        if (lastAction == Surrender) surrenderHand(hand)
                        else if (isInstantPayoutAt21() && hand.playingTotal() == 21) {
                            payoutHand(hand, getWinnerPayoutRatio(hand))
                        }

                        playedHands.add(hand)
                    }
                }
            }

            // Dealer reveals hole card after all players have played
            dealerSeat.revealHoleCard()

            // Only play dealer's hands if there are unresolved hands
            if (activePlayerSeats.any { it.getHands().any { !it.isResolved() } }) playHand(dealerSeat, dealerSeat.getHand(0))

            // Distribute winnings to winning players
            payWinners(dealerSeat.getHand(0), activePlayerSeats)
        }
    }

    private fun resolvePlayerBlackjacks(playerSeats: List<PlayerSeat>) {
        playerSeats.filter { it.getHand(0).isBlackjack() }.forEach { payoutHand(it.getHand(0), getBlackjackPayoutRatio()) }
    }

    fun discardHands() {
        seats.forEach { it.discardHands() }
        dealerSeat.discardHands()
    }

    fun shuffle() {
        shoe.shuffle()
        shoe.takeTop()
    }

    fun addPlayer(player: Player, @IntRange(from = 0, to = 7) seatIndex: Int): PlayerSeat = seats[seatIndex].apply { sit(player) }

    fun addToPreBet(player: Player, chip: Chip) {
        playerSeat(player)?.addToPreBet(chip)
    }

    fun getPreBet(player: Player) = playerSeat(player)?.getPreBet()

    fun getPreBetTotal(player: Player) = getPreBet(player)?.let { ChipUtil.getTotalRaw(it) }

    fun onPlayerHandsChanged(player: Player, operation: ((List<Hand<*>>) -> Unit)) {
        seats.firstOrNull { it.person === player }!!.onHandsChanged { operation.invoke(it) }
    }

    fun onDealerChanged(operation: ((List<Hand<*>>) -> Unit)) {
        dealerSeat.onHandsChanged { operation.invoke(it) }
    }

    private fun playerSeat(player: Player): PlayerSeat? {
        val seatOptional = seats.stream().filter { it is PlayerSeat && it.person == player }.findFirst()
        return if (seatOptional.isPresent) seatOptional.get() else null
    }

    private fun playHand(seat: Seat<*, *>, hand: Hand<*>) = with(hand) {
        var action: Hand.Action = Hit
        while (!isResolved() && playingTotal() != 21 && action != Stay && action != Surrender && action != Split) {
            val availableActions = getAvailableActions(seat, this, action)
            action = seat.person!!.playHand(getCards().map { it.first }, availableActions)
            if (!availableActions.contains(action)) throw RuntimeException("Invalid action: ${action.name}")
            when (action) {
                Hit -> addCard(shoe.takeTop())
                DoubleDown -> {
                    (seat as? PlayerSeat)?.run {
                        doubleDown(hand as PlayerHand)
                        addCard(shoe.takeTop())
                    }
                }
                Split -> (seat as? PlayerSeat)?.splitHand(hand as PlayerHand)
                Stay, Surrender -> { /* do nothing */
                }
            }
        }

        return@with action
    }

    private fun payInsurance(activeSeats: List<PlayerSeat>) {
        activeSeats.filter { it.hasBetInsurance() }.forEach { payoutBet(it.person!!, it.getInsuranceBet(), getInsurancePayoutRatio()) }
    }

    private fun payWinners(dealerHand: DealerHand, activeSeats: List<PlayerSeat>) {
        activeSeats.flatMap { it.getHands() }.filterNot { it.isBusted() || it.isResolved() }.forEach {
            if (it.playingTotal() > dealerHand.playingTotal() || dealerHand.isBusted()) payoutHand(it, getWinnerPayoutRatio(it))
            else if (it.playingTotal() == dealerHand.playingTotal()) pushHand(it)
        }
    }

    private fun surrenderHand(hand: PlayerHand) {
        with(hand) {
            owner.addAllChips(ChipUtil.getChips(getSurrenderAmount(betTotal())))
            markResolved()
        }
    }

    private fun getSurrenderAmount(betTotal: Int): Int {
        val amount = (betTotal * getSurrenderPayoutRatio()).toInt()
        return amount + amount % 100
    }

    private fun payoutHand(hand: PlayerHand, payoutRatio: Float) {
        payoutBet(hand.owner, hand.getBet(), payoutRatio)
        hand.markResolved()
    }

    private fun payoutBet(player: Player, bet: List<Chip>, payoutRatio: Float) {
        player.addAllChips(bet)
        player.addAllChips(ChipUtil.getChips((ChipUtil.getTotalRaw(bet) * payoutRatio).toInt()))
    }

    private fun pushHand(hand: PlayerHand) {
        hand.owner.addAllChips(hand.getBet())
        hand.markResolved()
    }

    private fun getAvailableActions(seat: Seat<*, *>, hand: Hand<*>, lastAction: Hand.Action): List<Hand.Action> {
        if (seat is DealerSeat) return arrayListOf(Hit, Stay)
        Pair(seat as PlayerSeat, hand as PlayerHand).letEach { playerSeat, playerHand ->
            if (lastAction == DoubleDown) return arrayListOf(Stay, Surrender)

            val actions = arrayListOf(Hit, Stay)
            if (canSurrender(playerSeat, playerHand)) actions.add(Surrender)
            if (playerSeat.canAffordDoubleDown(playerHand) && canDoubleDown(playerHand)) actions.add(DoubleDown)
            if (playerSeat.canAffordSplit(playerHand) && canSplit(playerSeat.splitCount, playerHand)) actions.add(Split)

            return actions
        }
    }

    abstract fun getBlackjackPayoutRatio(): Float

    abstract fun getWinnerPayoutRatio(hand: PlayerHand): Float

    abstract fun getSurrenderPayoutRatio(): Float

    abstract fun getInsurancePayoutRatio(): Float

    abstract fun playerBlackjackBeatsDealerBlackjack(): Boolean

    abstract fun isInstantPayoutAt21(): Boolean

    abstract fun canDoubleDown(hand: PlayerHand): Boolean

    abstract fun canSurrender(seat: PlayerSeat, hand: PlayerHand): Boolean

    abstract fun canSplit(splitCount: Int, hand: PlayerHand): Boolean

    private inline fun <A, B, R> Pair<A, B>.letEach(block: (A, B) -> R) = block.invoke(first, second)
}