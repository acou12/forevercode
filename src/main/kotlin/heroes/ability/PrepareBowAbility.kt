package heroes.ability

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import util.ChatUtil.sendGameMessage
import util.Timer

abstract class PrepareBowAbility(
    val time: Long,
    val name: String,
    val player: Player,
) : Ability() {
    val timer = Timer(time)
    var notified = true

    override fun use() {
        if (timer.done()) {
            timer.reset()
            player.sendGameMessage("You prepared ", name, ".")
            PrepareManager.playerMap[player.uniqueId] =
                PrepareManager.PreparationData(::onHitEntity, ::onHitBlock)
        } else player.sendMessage(AbilityUtil.getRemainingString(timer, name))
    }

    override fun tick() {
        if (!notified && timer.done()) {
            notified = true
            player.sendGameMessage("You can use ", name, ".")
        }
    }
    abstract fun onHitEntity(entity: Entity)
    abstract fun onHitBlock(location: Location)
}
