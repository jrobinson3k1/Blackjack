package us.jasonrobinson.fun21carnival.data.model

class DealerHand(dealer: Dealer) : Hand<Dealer>(dealer) {

    override fun shouldRevealCard(card: Card) = !cards.isEmpty()

    fun revealHoleCard() {
        cards[0] = cards[0].copy(second = true)
        notifyHandChanged()
    }
}