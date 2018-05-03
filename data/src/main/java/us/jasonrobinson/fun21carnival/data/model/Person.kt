package us.jasonrobinson.fun21carnival.data.model

abstract class Person(val name: String) {

    abstract fun playHand(cards: List<Card>, availableActions: List<Hand.Action>): Hand.Action
}