package heroes.ability

import kotlin.math.ceil
import org.bukkit.entity.Player
import util.ChatUtil

class TimedAbility(val time: Long, val name: String, val player: Player, val ability: () -> Unit) :
    Ability() {

    var lastUse: Long = 0
    var notified = true

    private fun elapsed() = System.currentTimeMillis() - lastUse

    override fun use() {
        val elapsed = elapsed()
        if (elapsed > time) {
            ability()
            lastUse = System.currentTimeMillis()
            notified = false
            player.sendMessage(ChatUtil.gameMessage("You used ", name, "."))
        } else {
            val remaining = ceil((time - elapsed) / 1000.0).toInt()
            player.sendMessage(
                ChatUtil.gameMessage(
                    "You cannot use ",
                    name,
                    " yet. Please wait ",
                    remaining,
                    " second${if (remaining == 1) "" else "s"}."
                )
            )
        }
    }

    override fun tick() {
        val elapsed = elapsed()
        if (elapsed > time && !notified) {
            notified = true
            player.sendMessage(ChatUtil.gameMessage("You can use ", name, "."))
        }
    }
}
