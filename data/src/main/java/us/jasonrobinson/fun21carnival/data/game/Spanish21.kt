package us.jasonrobinson.fun21carnival.data.game

import us.jasonrobinson.fun21carnival.data.model.*

class Spanish21(dealer: Dealer, minimumBet: Int, numOfDecks: Int) : Blackjack(Shoe(Deck.createSpanish21(), numOfDecks), dealer, minimumBet) {

    override fun getBlackjackPayoutRatio() = 3f / 2f

    override fun getWinnerPayoutRatio(hand: PlayerHand): Float {
        if (hand.playingTotal() == 21) {
            val cardRanks = hand.getCards().map { it.first.rank }
            if (cardRanks.count { it == Rank.Seven } == 3 || // 7-7-7
                    cardRanks.containsAll(listOf(Rank.Six, Rank.Seven, Rank.Eight)) || // 6-7-8
                    cardRanks.count() >= 5) // 5+ cards
                return 3f / 2f
        }

        return 1f
    }

    override fun getSurrenderPayoutRatio() = 1f / 2f

    override fun getInsurancePayoutRatio() = 2f

    override fun isInstantPayoutAt21() = true

    override fun playerBlackjackBeatsDealerBlackjack() = true

    override fun canSurrender(seat: PlayerSeat, hand: PlayerHand) = seat.splitCount == 0 && hand.getCards().size == 2

    override fun canDoubleDown(hand: PlayerHand) = !hand.doubledDown

    override fun canSplit(splitCount: Int, hand: PlayerHand) = with(hand.getCards()) { size == 2 && get(0).first.rank == get(1).first.rank } &&
            splitCount < 4

}