package us.jasonrobinson.fun21carnival.util

import android.content.Context
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.ImageView
import us.jasonrobinson.fun21carnival.R
import us.jasonrobinson.fun21carnival.data.model.Card
import us.jasonrobinson.fun21carnival.data.model.Rank
import us.jasonrobinson.fun21carnival.data.model.Suit
import kotlin.math.roundToInt

class CardUtil {

    companion object {
        fun createCardImageView(context: Context, card: Pair<Card, Boolean>, depth: Int): ImageView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96f, context.resources.displayMetrics).roundToInt(),
                    FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = depth * context.convertToDp(18)
            }
            adjustViewBounds = true
            setImageResource(if (!card.second) R.drawable.back else getResId(card.first))
        }

        private fun getResId(card: Card): Int = when (card.rank to card.suit) {
            Rank.Ace to Suit.Spades -> R.drawable.ace_of_spades
            Rank.Two to Suit.Spades -> R.drawable.two_of_spades
            Rank.Three to Suit.Spades -> R.drawable.three_of_spades
            Rank.Four to Suit.Spades -> R.drawable.four_of_spades
            Rank.Five to Suit.Spades -> R.drawable.five_of_spades
            Rank.Six to Suit.Spades -> R.drawable.six_of_spades
            Rank.Seven to Suit.Spades -> R.drawable.seven_of_spades
            Rank.Eight to Suit.Spades -> R.drawable.eight_of_spades
            Rank.Nine to Suit.Spades -> R.drawable.nine_of_spades
            Rank.Ten to Suit.Spades -> R.drawable.ten_of_spades
            Rank.Jack to Suit.Spades -> R.drawable.jack_of_spades
            Rank.Queen to Suit.Spades -> R.drawable.queen_of_spades
            Rank.King to Suit.Spades -> R.drawable.king_of_spades

            Rank.Ace to Suit.Clubs -> R.drawable.ace_of_clubs
            Rank.Two to Suit.Clubs -> R.drawable.two_of_clubs
            Rank.Three to Suit.Clubs -> R.drawable.three_of_clubs
            Rank.Four to Suit.Clubs -> R.drawable.four_of_clubs
            Rank.Five to Suit.Clubs -> R.drawable.five_of_clubs
            Rank.Six to Suit.Clubs -> R.drawable.six_of_clubs
            Rank.Seven to Suit.Clubs -> R.drawable.seven_of_clubs
            Rank.Eight to Suit.Clubs -> R.drawable.eight_of_clubs
            Rank.Nine to Suit.Clubs -> R.drawable.nine_of_clubs
            Rank.Ten to Suit.Clubs -> R.drawable.ten_of_clubs
            Rank.Jack to Suit.Clubs -> R.drawable.jack_of_clubs
            Rank.Queen to Suit.Clubs -> R.drawable.queen_of_clubs
            Rank.King to Suit.Clubs -> R.drawable.king_of_clubs

            Rank.Ace to Suit.Hearts -> R.drawable.ace_of_hearts
            Rank.Two to Suit.Hearts -> R.drawable.two_of_hearts
            Rank.Three to Suit.Hearts -> R.drawable.three_of_hearts
            Rank.Four to Suit.Hearts -> R.drawable.four_of_hearts
            Rank.Five to Suit.Hearts -> R.drawable.five_of_hearts
            Rank.Six to Suit.Hearts -> R.drawable.six_of_hearts
            Rank.Seven to Suit.Hearts -> R.drawable.seven_of_hearts
            Rank.Eight to Suit.Hearts -> R.drawable.eight_of_hearts
            Rank.Nine to Suit.Hearts -> R.drawable.nine_of_hearts
            Rank.Ten to Suit.Hearts -> R.drawable.ten_of_hearts
            Rank.Jack to Suit.Hearts -> R.drawable.jack_of_hearts
            Rank.Queen to Suit.Hearts -> R.drawable.queen_of_hearts
            Rank.King to Suit.Hearts -> R.drawable.king_of_hearts

            Rank.Ace to Suit.Diamonds -> R.drawable.ace_of_diamonds
            Rank.Two to Suit.Diamonds -> R.drawable.two_of_diamonds
            Rank.Three to Suit.Diamonds -> R.drawable.three_of_diamonds
            Rank.Four to Suit.Diamonds -> R.drawable.four_of_diamonds
            Rank.Five to Suit.Diamonds -> R.drawable.five_of_diamonds
            Rank.Six to Suit.Diamonds -> R.drawable.six_of_diamonds
            Rank.Seven to Suit.Diamonds -> R.drawable.seven_of_diamonds
            Rank.Eight to Suit.Diamonds -> R.drawable.eight_of_diamonds
            Rank.Nine to Suit.Diamonds -> R.drawable.nine_of_diamonds
            Rank.Ten to Suit.Diamonds -> R.drawable.ten_of_diamonds
            Rank.Jack to Suit.Diamonds -> R.drawable.jack_of_diamonds
            Rank.Queen to Suit.Diamonds -> R.drawable.queen_of_diamonds
            Rank.King to Suit.Diamonds -> R.drawable.king_of_diamonds
            else -> 0
        }

        private fun Context.convertToDp(value: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(),
                resources.displayMetrics).roundToInt()
    }
}