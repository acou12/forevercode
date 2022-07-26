package heroes.ability

import java.util.*
import org.bukkit.Location
import org.bukkit.entity.Arrow
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent

object PrepareManager : Listener {
    data class PreparationData(
        val onHitEntity: (Entity) -> Unit,
        val onHitBlock: (Location) -> Unit
    )

    val playerMap: MutableMap<UUID, PreparationData> = mutableMapOf()

    @EventHandler
    fun arrowHit(event: ProjectileHitEvent) {
        val arrow = event.entity as? Arrow ?: return
        val player = arrow.shooter as? Player ?: return
        val preparationData = playerMap[player.uniqueId] ?: return
        if (event.hitEntity != null) {
            preparationData.onHitEntity(event.hitEntity!!)
            playerMap.remove(player.uniqueId)
        } else if (event.hitBlock != null) {
            preparationData.onHitBlock(event.hitBlock!!.location)
            playerMap.remove(player.uniqueId)
        } else {
            // uh how
        }
    }
}
