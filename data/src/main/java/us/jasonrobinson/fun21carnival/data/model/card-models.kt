package us.jasonrobinson.fun21carnival.data.model

import android.support.annotation.IntRange
import java.util.*
import kotlin.collections.ArrayList

enum class Suit {
    Hearts,
    Diamonds,
    Spades,
    Clubs
}

enum class Rank(vararg val value: Int) {
    Two(2),
    Three(3),
    Four(4),
    Five(5),
    Six(6),
    Seven(7),
    Eight(8),
    Nine(9),
    Ten(10),
    Jack(10),
    Queen(10),
    King(10),
    Ace(1, 11)
}

data class Card(val suit: Suit, val rank: Rank) {
    override fun toString() = rank.name + suit.name
}

data class Deck(val cards: List<Card>) {

    companion object Factory {
        fun createSpanish21(): Deck {
            return Deck(allCards().filterNot { it.rank == Rank.King })
        }

        fun createStandard(): Deck {
            return Deck(allCards())
        }

        private fun allCards(): List<Card> {
            val cards = arrayListOf<Card>()
            Suit.values().forEach { suit ->
                Rank.values().forEach { rank ->
                    cards.add(Card(suit, rank))
                }
            }

            return cards
        }
    }
}

data class Shoe(private val deck: Deck, @IntRange(from = 1, to = Long.MAX_VALUE) private val amount: Int) {

    private val cards = ArrayDeque<Card>()
    val size: Int = deck.cards.size * amount

    fun shuffle() {
        val list = ArrayList<Card>()
        repeat(amount, { list.addAll(deck.copy().cards) })
        val shuffledCards = list.shuffled()

        cards.clear()
        cards.addAll(shuffledCards)
    }

    fun takeTop(): Card = cards.pop()

    fun remaining() = cards.size
}