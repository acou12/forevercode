package heroes.ability

import org.bukkit.entity.Player
import util.ChatUtil.sendGameMessage
import util.Timer

class TimedAbility(val time: Long, val name: String, val player: Player, val ability: () -> Unit) :
    Ability() {

    val timer = Timer(time)
    var notified = true

    override fun use() {
        if (timer.done()) {
            timer.reset()
            ability()
            notified = false
            player.sendGameMessage("You used ", name, ".")
        } else player.sendMessage(AbilityUtil.getRemainingString(timer, name))
    }

    override fun tick() {
        if (timer.done() && !notified) {
            notified = true
            player.sendGameMessage("You can use ", name, ".")
        }
    }
}
