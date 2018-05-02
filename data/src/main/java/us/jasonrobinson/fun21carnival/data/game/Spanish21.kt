package us.jasonrobinson.fun21carnival.data.game

import us.jasonrobinson.fun21carnival.data.model.*

class Spanish21(dealer: Dealer, minimumBet: Int, numOfDecks: Int) : Blackjack(Shoe(Deck.createSpanish21(), numOfDecks), dealer, minimumBet) {

    override fun getBlackjackPayoutRatio() = 3f / 2f

    override fun getWinnerPayoutRatio() = 1f

    override fun getSurrenderPayoutRatio() = 1f / 2f

    override fun isInstantPayoutAt21() = true

    override fun canSurrender(seat: PlayerSeat, hand: PlayerHand) = seat.getHands().size == 1

    override fun canDoubleDown(hand: PlayerHand) = !hand.doubledDown

    override fun canSplit(splitCount: Int, hand: PlayerHand) = with(hand.getCards()) { size == 2 && get(0).first.rank == get(1).first.rank } &&
            splitCount < 4

}