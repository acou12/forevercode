package heroes.unique.uniques

import heroes.unique.Unique
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import util.ChatUtil.sendGameMessage

object AwesomeFeatherOfCoolness : Unique() {
    override val name: String = "Awesome Feather of Coolness"
    override val lore: String = "Cancels all fall damage."
    override val type: Material = Material.FEATHER

    @EventHandler
    fun onFall(event: EntityDamageEvent) =
        inventoryItemGuard(event.entity) {
            event.isCancelled = true
            it.sendGameMessage(
                "Your ",
                "Awesome Feather of Coolness",
                " cancelled your fall damage."
            )
        }
}
