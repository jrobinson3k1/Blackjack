package us.jasonrobinson.fun21carnival.data.model

import us.jasonrobinson.fun21carnival.data.util.ChipUtil

class PlayerSeat : Seat<Player, PlayerHand>() {

    private var preBet = arrayListOf<Chip>()
    private var insuranceBet = arrayListOf<Chip>()

    var splitCount = 0
        private set

    override fun onHandsDiscarded() {
        preBet.clear()
        insuranceBet.clear()
        splitCount = 0
    }

    fun addToPreBet(chip: Chip) {
        if (!person!!.getChips().contains(chip)) throw RuntimeException("Insufficient funds to pre-bet")
        preBet.add(chip)
        person!!.removeChip(chip)
    }

    fun getPreBet() = List(preBet.size, { preBet[it] })

    fun getInsuranceBet() = List(insuranceBet.size, { insuranceBet[it] })

    fun canAffordDoubleDown(hand: PlayerHand) = person!!.findChipsForTotal(hand.betTotal()) != null

    fun doubleDown(hand: PlayerHand) {
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

    fun betInsurance() {
        val insuranceChips = person!!.findChipsForTotal(ChipUtil.getTotalRaw(preBet) / 2)
                ?: throw RuntimeException("Insufficient funds to bet insurance")
        insuranceBet.addAll(insuranceChips)
        person!!.removeAllChips(insuranceChips)
    }

    fun hasBetInsurance() = !insuranceBet.isEmpty()

    override fun createHand() = PlayerHand(person!!, preBet)

    override fun isPlaying() = person != null && !preBet.isEmpty()
}