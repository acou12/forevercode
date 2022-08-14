package util

import org.bukkit.Bukkit

object SchedulerUtil {
    fun <T> delayedFor(ticks: Long, range: Iterable<T>, runnable: (T) -> Unit) {
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
