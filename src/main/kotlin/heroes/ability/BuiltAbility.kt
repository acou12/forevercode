package heroes.ability

import org.bukkit.entity.Player
import util.ChatUtil.sendGameMessage
import util.Timer

class BuiltAbility(
    val time: Long,
    val maxBuildTime: Long?,
    val name: String,
    val player: Player,
    val buildStart: () -> Unit,
    val buildTick: () -> Unit,
    val buildEnd: (buildTime: Long) -> Unit
) : Ability() {
    var prepared = false
    var using = false
    var buildStartTime = 0L
    val timer = Timer(time)

    override fun use() {
        if (timer.done()) {
            player.sendGameMessage("You prepared ", name, ".")
        } else player.sendMessage(AbilityUtil.getRemainingString(timer, name))
    }

    override fun tick() {
        if (prepared && player.isSneaking) {
            prepared = false
            using = true
            buildStart()
            buildStartTime = System.currentTimeMillis()
        } else if (using) {
            if (!player.isSneaking) {
                using = false
                buildEnd(System.currentTimeMillis() - buildStartTime)
            } else buildTick()
        }
    }
}