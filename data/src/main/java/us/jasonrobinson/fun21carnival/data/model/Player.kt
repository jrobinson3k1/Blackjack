package us.jasonrobinson.fun21carnival.data.model

open class Player(name: String, private val eventListener: EventListener) : Person(name) {

    private val chips: ArrayList<Chip> = arrayListOf()

    override fun playHand(hand: Hand<*>, availableActions: List<Hand.Action>) = eventListener.onPlayHand(hand, availableActions)

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

    fun findChipForDenomination(denomination: Int) = chips.firstOrNull { it.denomination == denomination }

    fun getChips() = List(chips.size, { chips[it] })

    interface EventListener {
        fun onPlayHand(hand: Hand<*>, availableActions: List<Hand.Action>): Hand.Action

        fun onBankChanged(player: Player, chips: List<Chip>)
    }
}