package us.jasonrobinson.fun21carnival.data.model

import java.util.stream.Collectors

abstract class Seat<P : Person, H : Hand<*>> {

    var person: P? = null
    protected val hands: ArrayList<H> = arrayListOf()
    private val handChangeListeners: ArrayList<((List<H>) -> Unit)> = arrayListOf()

    fun newHand(initialCard: Card? = null): H = createHand().also {
        it.onHandChangedListener = object:Hand.OnHandChangedListener {
            override fun onHandChanged(hand: Hand<*>) {
                notifyHandChanged()
            }
        }
        if (initialCard != null) it.addCard(initialCard)
        hands.add(it)
    }

    fun discardHand(hand: H) {
        hands.remove(hand)
        notifyHandChanged()
    }

    fun discardHands() {
        hands.clear()
        notifyHandChanged()
        onHandsDiscarded()
    }

    fun getHand(index: Int) = hands[index]

    fun getHands() = List(hands.size, { hands[it] })

    fun handsSize() = hands.size

    fun sit(person: P) {
        this.person = person
    }

    fun stand() {
        this.person = null
    }

    fun onHandsChanged(operation: (List<H>) -> Unit) {
        handChangeListeners.add(operation)
    }

    protected fun notifyHandChanged() {
        handChangeListeners.forEach { it.invoke(hands.stream().collect(Collectors.toList())) }
    }

    protected open fun onHandsDiscarded() {}

    protected abstract fun createHand(): H

    abstract fun isPlaying(): Boolean
}