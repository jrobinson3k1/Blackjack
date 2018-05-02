package us.jasonrobinson.fun21carnival.data.model

import java.util.stream.Collectors

abstract class Hand<out T : Person>(val owner: T) {

    private var resolved = false
    protected val cards: ArrayList<Pair<Card, Boolean>> = arrayListOf()

    var onHandChangedListener: OnHandChangedListener? = null

    fun addCard(card: Card) {
        cards.add(Pair(card, shouldRevealCard(card)))
        notifyHandChanged()
    }

    fun getCards(): List<Pair<Card, Boolean>> = List(cards.size, { cards[it] })

    fun cardTotal(): List<Int> {
        val totals = arrayListOf(0)
        val cardValues = cards.stream()
                .filter { it.second }
                .map { it.first.rank.value.toList() }
                .collect(Collectors.toList())

        cardValues.forEach { values ->
            if (values.size == 1) totals.forEachIndexed { index, total -> totals[index] = total + values[0] }
            else {
                val splitTotals = ArrayList(totals).apply { forEachIndexed { index, total -> set(index, total + values[0]) } }
                totals.forEachIndexed { index, total -> totals[index] = total + values[1] }
                totals.addAll(splitTotals)
            }
        }

        return totals.stream().distinct().collect(Collectors.toList()).sorted()
    }

    fun markResolved() {
        resolved = true
    }

    fun isBusted() = cardTotal().all { it > 21 }

    fun isResolved() = isBusted() || resolved

    protected fun notifyHandChanged() {
        onHandChangedListener?.onHandChanged(this)
    }

    abstract fun shouldRevealCard(card: Card): Boolean

    interface OnHandChangedListener {
        fun onHandChanged(hand: Hand<*>)
    }

    enum class Action {
        Hit,
        Stay,
        DoubleDown,
        Split,
        Surrender
    }
}