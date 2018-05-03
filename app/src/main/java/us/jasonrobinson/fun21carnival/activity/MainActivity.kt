package us.jasonrobinson.fun21carnival.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import io.reactivex.subjects.PublishSubject
import us.jasonrobinson.fun21carnival.R
import us.jasonrobinson.fun21carnival.data.game.Blackjack
import us.jasonrobinson.fun21carnival.data.game.Spanish21
import us.jasonrobinson.fun21carnival.data.model.Chip
import us.jasonrobinson.fun21carnival.data.model.Hand
import us.jasonrobinson.fun21carnival.data.model.Hand.Action.*
import us.jasonrobinson.fun21carnival.data.model.HitSoft17Dealer
import us.jasonrobinson.fun21carnival.data.model.Player
import us.jasonrobinson.fun21carnival.data.util.ChipUtil
import us.jasonrobinson.fun21carnival.view.SeatView
import java.io.OutputStreamWriter

class MainActivity : AppCompatActivity() {

    private lateinit var nextHandButton: Button
    private lateinit var dealButton: Button
    private lateinit var betLayout: View
    private lateinit var handControllerLayout: View
    private lateinit var hitButton: Button
    private lateinit var stayButton: Button
    private lateinit var doubleDownButton: Button
    private lateinit var splitButton: Button
    private lateinit var surrenderButton: Button
    private lateinit var dealerSeatView: SeatView
    private lateinit var playerSeatView: SeatView
    private lateinit var betTextView: TextView
    private lateinit var bankTextView: TextView
    private lateinit var insuranceControllerLayout: View

    private lateinit var twoFiftyCount: TextView
    private lateinit var fiveCount: TextView
    private lateinit var twentyFiveCount: TextView
    private lateinit var oneHundredCount: TextView

    private val blackjack: Blackjack = Spanish21(HitSoft17Dealer(), 500, 8)
    private val player = Player("Jason", object : Player.EventListener {

        override fun onBankChanged(player: Player, chips: List<Chip>) {
            runOnUiThread {
                bankTextView.text = getString(R.string.bank, ChipUtil.getTotalDollars(chips))
                betTextView.text = getString(R.string.bet, ChipUtil.getTotalDollars(blackjack.getPreBet(player).orEmpty()))
                updateBank(chips)
            }
        }

        override fun onPlayHand(hand: Hand<*>, availableActions: List<Hand.Action>): Hand.Action {
            runOnUiThread({ showActions(availableActions) })
            return actionSubject.blockingFirst()
        }

        override fun takeInsurance(): Boolean {
            runOnUiThread { showInsurance() }
            return insuranceSubject.blockingFirst()
        }
    })
    private var bet: ArrayList<Chip> = arrayListOf()

    val actionSubject: PublishSubject<Hand.Action> = PublishSubject.create()
    val insuranceSubject: PublishSubject<Boolean> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defhandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            run {
                try {
                    val stacktrace = Log.getStackTraceString(throwable)
                    OutputStreamWriter(openFileOutput("crash.txt", Context.MODE_PRIVATE)).use {
                        it.write(stacktrace)
                    }
                } finally {
                    defhandler.uncaughtException(thread, throwable)
                }
            }
        }

        setContentView(R.layout.activity_main)

        nextHandButton = findViewById(R.id.nextHand)
        dealButton = findViewById(R.id.deal)
        betLayout = findViewById(R.id.betLayout)
        handControllerLayout = findViewById(R.id.handController)
        hitButton = findViewById(R.id.hit)
        stayButton = findViewById(R.id.stay)
        doubleDownButton = findViewById(R.id.doubleDown)
        splitButton = findViewById(R.id.split)
        surrenderButton = findViewById(R.id.surrender)
        dealerSeatView = findViewById(R.id.dealerSeat)
        playerSeatView = findViewById(R.id.playerSeat)
        betTextView = findViewById(R.id.bet)
        bankTextView = findViewById(R.id.bank)
        insuranceControllerLayout = findViewById(R.id.insuranceController)

        twoFiftyCount = findViewById(R.id.TwoFiftyCount)
        fiveCount = findViewById(R.id.FiveCount)
        twentyFiveCount = findViewById(R.id.TwentyCount)
        oneHundredCount = findViewById(R.id.OneHundredCount)

        for (i in 1..20) player.addAllChips(listOf(Chip.five()))
        blackjack.addPlayer(player, 0)
        blackjack.onPlayerHandsChanged(player, { runOnUiThread { playerSeatView.refresh(it) } })
        blackjack.onDealerChanged { runOnUiThread { dealerSeatView.refresh(it) } }
        blackjack.shuffle()

        nextHandButton.setOnClickListener {
            it.visibility = View.GONE
            dealButton.visibility = View.VISIBLE
            dealButton.isEnabled = false
            betLayout.visibility = View.VISIBLE
            blackjack.discardHands()
            betTextView.text = getString(R.string.bet, 0f)
        }

        dealButton.setOnClickListener {
            arrayOf(betLayout, nextHandButton, dealButton).forEach { it.visibility = View.GONE }

            Thread({
                blackjack.deal()
                runOnUiThread({
                    bet.clear()
                    handControllerLayout.visibility = View.GONE
                    insuranceControllerLayout.visibility = View.GONE

                    if (player.getChips().isEmpty()) {
                        Snackbar.make(playerSeatView, "You suck.", Snackbar.LENGTH_INDEFINITE).apply {
                            setAction("Restart", {
                                startActivity(Intent(this@MainActivity, MainActivity::class.java))
                                finish()
                            })
                        }.show()
                    } else nextHandButton.visibility = View.VISIBLE
                })
            }).start()
        }

        findViewById<Button>(R.id.betTwoFifty).setOnClickListener { bet(250) }
        findViewById<Button>(R.id.betFive).setOnClickListener { bet(500) }
        findViewById<Button>(R.id.betTwentyFive).setOnClickListener { bet(2500) }
        findViewById<Button>(R.id.betOneHundred).setOnClickListener { bet(10000) }

        hitButton.setOnClickListener { actionSubject.onNext(Hit) }
        stayButton.setOnClickListener { actionSubject.onNext(Stay) }
        doubleDownButton.setOnClickListener { actionSubject.onNext(DoubleDown) }
        splitButton.setOnClickListener { actionSubject.onNext(Split) }
        surrenderButton.setOnClickListener { actionSubject.onNext(Surrender) }

        findViewById<Button>(R.id.takeInsurance).setOnClickListener { insuranceSubject.onNext(true) }
        findViewById<Button>(R.id.passInsurance).setOnClickListener { insuranceSubject.onNext(false) }
    }

    private fun showActions(actions: List<Hand.Action>) {
        handControllerLayout.visibility = View.VISIBLE
        insuranceControllerLayout.visibility = View.GONE

        hitButton.visibility = if (actions.contains(Hit)) View.VISIBLE else View.GONE
        stayButton.visibility = if (actions.contains(Stay)) View.VISIBLE else View.GONE
        doubleDownButton.visibility = if (actions.contains(DoubleDown)) View.VISIBLE else View.GONE
        splitButton.visibility = if (actions.contains(Split)) View.VISIBLE else View.GONE
        surrenderButton.visibility = if (actions.contains(Surrender)) View.VISIBLE else View.GONE
    }

    private fun showInsurance() {
        insuranceControllerLayout.visibility = View.VISIBLE
    }

    private fun updateBank(chips: List<Chip>) {
        twoFiftyCount.text = chips.count { it.denomination == 250 }.toString()
        fiveCount.text = chips.count { it.denomination == 500 }.toString()
        twentyFiveCount.text = chips.count { it.denomination == 2500 }.toString()
        oneHundredCount.text = chips.count { it.denomination == 10000 }.toString()
    }

    private fun bet(denomination: Int) {
        val chip = player.findChipForDenomination(denomination)
        if (chip != null) {
            blackjack.addToPreBet(player, chip)
            dealButton.isEnabled = blackjack.getPreBetTotal(player) ?: 0 >= blackjack.minimumBet
        }
    }
}
