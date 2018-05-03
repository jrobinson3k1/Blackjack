package us.jasonrobinson.fun21carnival.data.model

class AIPlayer(name: String) : Player(name, object : EventListener {
    override fun takeInsurance(): Boolean = false

    override fun onPlayHand(cards: List<Card>, availableActions: List<Hand.Action>): Hand.Action {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBankChanged(player: Player, chips: List<Chip>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
})