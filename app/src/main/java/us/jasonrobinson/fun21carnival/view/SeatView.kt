package us.jasonrobinson.fun21carnival.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import us.jasonrobinson.fun21carnival.R
import us.jasonrobinson.fun21carnival.data.model.Hand
import us.jasonrobinson.fun21carnival.util.CardUtil

class SeatView : LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        orientation = VERTICAL
    }

    fun reset() {
        removeAllViews()
    }

    fun refresh(hands: List<Hand<*>>) {
        removeAllViews()
        hands.forEach { hand ->
            inflate(context, R.layout.hand, null).apply {
                findViewById<ViewGroup>(R.id.handLayout).apply {
                    hand.getCards().forEachIndexed { index, card ->
                        addView(CardUtil.createCardImageView(context, card, index))
                    }
                }

                findViewById<TextView>(R.id.handTotal).apply {
                    text = hand.getTotalDisplay()
                    setTextColor(if (hand.isBusted()) Color.RED else Color.WHITE)
                }

                addView(this)
            }
        }
    }

    private fun Hand<*>.getTotalDisplay(): String {
        if (cardTotal().isEmpty()) return ""

        val totals = cardTotal().filter { it <= 21 }
        return if (totals.isEmpty()) cardTotal().min().toString() else totals.joinToString(separator = ", ")
    }
}