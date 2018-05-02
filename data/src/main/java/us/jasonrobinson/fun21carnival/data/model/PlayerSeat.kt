package us.jasonrobinson.fun21carnival.data.model

class PlayerSeat : Seat<Player, PlayerHand>() {

    private var preBet = arrayListOf<Chip>()

    var splitCount = 0
        private set

    override fun onHandsDiscarded() {
        preBet.clear()
        splitCount = 0
    }

    fun addToPreBet(chip: Chip) {
        if (!person!!.getChips().contains(chip)) throw RuntimeException("Insufficient funds")
        preBet.add(chip)
        person!!.removeChip(chip)
    }

    fun getPreBet() = List(preBet.size, { preBet[it] })

    fun canAffordDoubleDown(hand: PlayerHand) = person!!.findChipsForTotal(hand.betTotal()) != null

    fun doubleBet(hand: PlayerHand) {
        val chips = person!!.findChipsForTotal(hand.betTotal()) ?: throw RuntimeException("Attempted to double down with insufficient chips")
        person!!.removeAllChips(chips)
        hand.addToBet(chips)
        hand.markDoubledDown()
    }

    fun canAffordSplit(hand: PlayerHand) = person!!.findChipsForTotal(hand.betTotal()) != null

    fun splitHand(hand: PlayerHand) {
        if (!canAffordSplit(hand)) throw RuntimeException("Attempted to split with incorrect cards or funds")

        val card1 = hand.getCards()[0].first
        val card2 = hand.getCards()[1].first
        newHand(card1)
        newHand(card2)
        discardHand(hand)

        splitCount++
    }

    override fun createHand() = PlayerHand(person!!, preBet)

    override fun isPlaying() = person != null && !preBet.isEmpty()
}