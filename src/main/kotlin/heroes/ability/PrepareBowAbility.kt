package heroes.ability

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import util.ChatUtil

class PrepareBowAbility(
    val time: Long,
    val name: String,
    val player: Player,
    val onHitEntity: (Entity) -> Unit,
    val onHitBlock: (Location) -> Unit
) : Ability() {
    var lastUse = 0L
    var notified = true

    override fun use() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUse > time) {
            player.sendMessage(ChatUtil.gameMessage("You prepared ", name, "."))
            lastUse = currentTime
            PrepareManager.playerMap[player.uniqueId] =
                PrepareManager.PreparationData(onHitEntity, onHitBlock)
        }
    }

    override fun tick() {
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastUse
        if (elapsed > time && !notified) {
            notified = true
            player.sendMessage(ChatUtil.gameMessage("You can use ", name, "."))
        }
    }
}
