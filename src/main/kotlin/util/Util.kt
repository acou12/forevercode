package util

import org.bukkit.Location

object Util {
    fun intermediateSteps(from: Location, to: Location, num: Int): List<Location> {
        val delta = to.clone().subtract(from)
        return (0..num).map { from.clone().add(delta.clone().multiply((it / num).toDouble())) }
    }
}
