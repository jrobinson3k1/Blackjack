package us.jasonrobinson.fun21carnival.data.model

import android.graphics.Color

class Chip(val denomination: Int, val color: Int) {

    companion object Factory {
        fun one() = Chip(100, Color.WHITE)
        fun twoAndHalf() = Chip(250, Color.parseColor("#ff69b4")) // Pink
        fun five() = Chip(500, Color.RED)
        fun twentyFive() = Chip(2500, Color.GREEN)
        fun oneHundred() = Chip(10000, Color.BLACK)

        fun forTotal(total: Int): List<Chip> {
            val chips = arrayListOf<Chip>()
            var runningTotal = total

            listOf(oneHundred(), twentyFive(), five(), twoAndHalf(), one()).forEach { chip ->
                val amount = Math.floor(runningTotal / chip.denomination.toDouble()).toInt()
                repeat(amount, {
                    runningTotal -= chip.denomination
                    chips.add(Chip(chip.denomination, chip.color))
                })
            }

            return chips
        }
    }
}