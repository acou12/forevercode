package util

import org.bukkit.Bukkit

object SchedulerUtil {
    fun delayedFor(ticks: Long, range: Iterable<Int>, runnable: (Int) -> Unit) {
        if (!range.none()) {
            Bukkit.getScheduler()
                .scheduleSyncDelayedTask(
                    Main.plugin,
                    {
                        runnable(range.first())
                        delayedFor(ticks, range.drop(1), runnable)
                    },
                    ticks
                )
        }
    }
}
