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

        // Check for blackjack
        activePlayerSeats.filter { it.getHand(0).playingTotal() == 21 }.forEach {
            payoutHand(it.getHand(0), getBlackjackPayoutRatio())
        }

        // Players play their hands
        activePlayerSeats.forEach {
            val operatedElements = arrayListOf<PlayerHand>()
            while (operatedElements.size < it.getHands().size) {
                for (hand in it.getHands()) {
                    if (!operatedElements.contains(hand)) {
                        val lastAction = playHand(it, hand)
                        if (lastAction == Surrender) surrenderHand(hand)
                        else if (isInstantPayoutAt21() && hand.playingTotal() == 21) {
                            payoutHand(hand, getWinnerPayoutRatio())
                        }

                        operatedElements.add(hand)
                    }
                }
            }
        }

        // Dealer reveals hole card after all players have played
        dealerSeat.revealHoleCard()

        // Only play dealers hands if there are unresolved hands
        if (activePlayerSeats.any { it.getHands().any { !it.isResolved() } }) playHand(dealerSeat, dealerSeat.getHand(0))

        // Distribute winnings to winning players
        payWinners(dealerSeat.getHand(0), activePlayerSeats)
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
            action = seat.person!!.playHand(this, availableActions)
            if (!availableActions.contains(action)) throw RuntimeException("Invalid action: ${action.name}")
            when (action) {
                Hit -> addCard(shoe.takeTop())
                DoubleDown -> {
                    (seat as? PlayerSeat)?.run {
                        doubleBet(hand as PlayerHand)
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

    private fun payWinners(dealerHand: DealerHand, activeSeats: List<PlayerSeat>) {
        activeSeats.flatMap { it.getHands() }.filterNot { it.isBusted() || it.isResolved() }.forEach {
            if (it.playingTotal() > dealerHand.playingTotal() || dealerHand.isBusted()) payoutHand(it, getWinnerPayoutRatio())
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
        with(hand) {
            owner.addAllChips(getBet())
            owner.addAllChips(ChipUtil.getChips((betTotal() * payoutRatio).toInt()))
            markResolved()
        }
    }

    private fun pushHand(hand: PlayerHand) {
        hand.owner.addAllChips(hand.getBet())
        hand.markResolved()
    }

    abstract fun getBlackjackPayoutRatio(): Float

    abstract fun getWinnerPayoutRatio(): Float

    abstract fun getSurrenderPayoutRatio(): Float

    abstract fun isInstantPayoutAt21(): Boolean

    abstract fun canDoubleDown(hand: PlayerHand): Boolean

    abstract fun canSurrender(seat: PlayerSeat, hand: PlayerHand): Boolean

    abstract fun canSplit(splitCount: Int, hand: PlayerHand): Boolean

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

    private fun Hand<*>.playingTotal() = cardTotal().filter { it <= 21 }.max() ?: cardTotal().min() ?: 0

    private inline fun <A, B, R> Pair<A, B>.letEach(block: (A, B) -> R) = block.invoke(first, second)
}