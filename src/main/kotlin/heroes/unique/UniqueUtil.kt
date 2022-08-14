package heroes.unique

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import util.ChatUtil.sendGameMessage

object UniqueUtil {
    enum class ConsumptionRequirement(val includes: (PlayerInventory) -> List<ItemStack>) {
        HAND({ listOf(it.itemInMainHand) }),
        HAND_OR_OFFHAND({ listOf(it.itemInOffHand) }),
        HOTBAR({ (it.contents.take(9)) }),
        INVENTORY({ it.contents.slice(9..27) })
    }
    fun consume(player: Player, unique: Unique, requirement: ConsumptionRequirement) {
        player.sendGameMessage("You consumed ", unique.name, ".")
        val pool =
            ConsumptionRequirement.values()
                .filter { it.ordinal <= requirement.ordinal }
                .flatMap { it.includes(player.inventory) }
        val item = pool.firstOrNull(unique::isItem) ?: return
        item.amount--
    }
}
