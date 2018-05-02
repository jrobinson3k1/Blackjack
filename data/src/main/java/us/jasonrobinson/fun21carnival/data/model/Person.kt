package us.jasonrobinson.fun21carnival.data.model

abstract class Person(val name: String) {

    abstract fun playHand(hand: Hand<*>, availableActions: List<Hand.Action>): Hand.Action
}