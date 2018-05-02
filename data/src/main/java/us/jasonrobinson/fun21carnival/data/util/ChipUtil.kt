package us.jasonrobinson.fun21carnival.data.util

import us.jasonrobinson.fun21carnival.data.model.Chip
import java.util.stream.Collectors

class ChipUtil {

    companion object {

        fun getTotalRaw(chips: List<Chip>): Int = chips.stream().collect(Collectors.summingInt { it.denomination })

        fun getTotalDollars(chips: List<Chip>) = getTotalRaw(chips) / 100f

        fun getChips(total: Int) = Chip.forTotal(total)
    }
}