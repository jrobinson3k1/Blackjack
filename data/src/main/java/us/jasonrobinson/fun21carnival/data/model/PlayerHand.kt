package us.jasonrobinson.fun21carnival.data.model

import java.util.stream.Collectors

class PlayerHand(player: Player, bet: List<Chip>) : Hand<Player>(player) {

    private val bet = ArrayList(bet)

    var doubledDown = false
        private set

    override fun shouldRevealCard(card: Card) = true

    fun addToBet(chip: Chip) {
        bet.add(chip)
    }

    fun addToBet(chips: List<Chip>) {
        bet.addAll(chips)
    }

    fun markDoubledDown() {
        doubledDown = true
    }

    fun getBet(): List<Chip> = List(bet.size, { bet[it] })

    fun betTotal(): Int = bet.stream().collect(Collectors.summingInt { it.denomination })
}