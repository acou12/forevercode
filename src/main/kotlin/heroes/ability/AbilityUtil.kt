package heroes.ability

import kotlin.math.ceil
import util.ChatUtil
import util.Timer

object AbilityUtil {
    fun getRemainingString(timer: Timer, name: String): String {
        val remaining = ceil((timer.time - timer.elapsed()) / 1000.0).toInt()
        return ChatUtil.gameMessage(
            "You cannot use ",
            name,
            " yet. Please wait ",
            remaining,
            " second${if (remaining == 1) "" else "s"}."
        )
    }
}
