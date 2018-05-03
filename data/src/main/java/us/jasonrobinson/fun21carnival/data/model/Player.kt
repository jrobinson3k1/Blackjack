package us.jasonrobinson.fun21carnival.data.model

open class Player(name: String, private val eventListener: EventListener) : Person(name) {

    private val chips: ArrayList<Chip> = arrayListOf()

    override fun playHand(cards: List<Card>, availableActions: List<Hand.Action>) = eventListener.onPlayHand(cards, availableActions)

    fun takeInsurance() = eventListener.takeInsurance()

    fun addChip(chip: Chip) {
        this.chips.add(chip)
        notifyBankChanged()
    }

    fun addAllChips(chips: List<Chip>) {
        this.chips.addAll(chips)
        notifyBankChanged()
    }

    fun removeChip(chip: Chip) {
        this.chips.remove(chip)
        notifyBankChanged()
    }

    fun removeChip(denomination: Int): Chip? {
        val chip = this.chips.findLast { it.denomination == denomination }
        if (chip != null) removeChip(chip)
        return chip
    }

    fun removeAllChips(chips: List<Chip>) {
        if (this.chips.containsAll(chips)) this.chips.removeAll(chips)
        notifyBankChanged()
    }

    private fun notifyBankChanged() {
        eventListener.onBankChanged(this, getChips())
    }

    fun findChipsForTotal(total: Int): List<Chip>? = with(arrayListOf<Chip>()) {
        var runningTotal = total
        chips.sortedByDescending { it.denomination }.forEach {
            if (runningTotal >= it.denomination) {
                add(it)
                runningTotal -= it.denomination
            }
        }

        if (runningTotal == 0) this else null
    }

    private fun breakDownChip(chip: Chip): List<Chip> {
        if (chip == Chip.one() || chip == Chip.twoAndHalf()) throw RuntimeException("Can't break down $1 or $2.50 chip")

        val chips = arrayListOf<Chip>()
        when (chip) {
            Chip.five() -> repeat(5, { chips.add(Chip.one()) })
            Chip.twentyFive() -> repeat(5, { chips.add(Chip.five()) })
            Chip.oneHundred() -> repeat(4, { chips.add(Chip.twentyFive()) })
            else -> throw RuntimeException("WHAT CHIP IS THIS!?!? denomination=${chip.denomination}")
        }

        return chips
    }

    fun findChipForDenomination(denomination: Int) = chips.firstOrNull { it.denomination == denomination }

    fun getChips() = List(chips.size, { chips[it] })

    interface EventListener {
        fun onPlayHand(cards: List<Card>, availableActions: List<Hand.Action>): Hand.Action

        fun takeInsurance(): Boolean

        fun onBankChanged(player: Player, chips: List<Chip>)
    }
}