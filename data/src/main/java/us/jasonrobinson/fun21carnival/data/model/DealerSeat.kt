package us.jasonrobinson.fun21carnival.data.model

class DealerSeat(dealer: Dealer) : Seat<Dealer, DealerHand>() {

    init {
        sit(dealer)
    }

    fun revealHoleCard() {
        hands[0].revealHoleCard()
    }

    override fun createHand() = DealerHand(person!!)

    override fun isPlaying() = true
}