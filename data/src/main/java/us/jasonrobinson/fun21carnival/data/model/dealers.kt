package us.jasonrobinson.fun21carnival.data.model

abstract class Dealer : Person("Dealer")

class HitSoft17Dealer : Dealer() {

    override fun playHand(hand: Hand<*>, availableActions: List<Hand.Action>): Hand.Action {
        Thread.sleep(750)
        return with(hand.cardTotal().filter { it <= 21 }) {
            if ((max() == 17 && min()!! < max()!!) || max()!! < 17) Hand.Action.Hit else Hand.Action.Stay
        }
    }
}