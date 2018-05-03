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

    fun cardTotal(includeHidden: Boolean = false) = Util.cardTotal(cards.filter { if (!includeHidden) it.second else true }.map { it.first })

    fun playingTotal(includeHidden: Boolean = false) = cardTotal(includeHidden).filter { it <= 21 }.max() ?: cardTotal().min() ?: 0

    fun isBlackjack() = cards.size == 2 && playingTotal(true) == 21

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

    companion object Util {
        fun cardTotal(cards: List<Card>): List<Int> {
            val totals = arrayListOf(0)
            val cardValues = cards.stream()
                    .map { it.rank.value.toList() }
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

        fun playingTotal(cards: List<Card>) = with(cardTotal(cards)) { filter { it <= 21 }.max() ?: min() ?: 0 }
    }
}