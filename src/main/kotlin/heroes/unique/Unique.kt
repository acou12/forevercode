package heroes.unique

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

abstract class Unique : Listener {
    abstract val name: String
    abstract val lore: String
    abstract val type: Material

    fun isItem(item: ItemStack) = ChatColor.stripColor(item.itemMeta?.displayName) == name

    open fun createItemStack(): ItemStack {
        val stack = ItemStack(type)
        val meta = stack.itemMeta!!
        meta.setDisplayName(ChatColor.RESET.toString() + ChatColor.GOLD + name)
        meta.lore =
            (lore)
                .split(" ")
                .chunked(6)
                .map { it.joinToString(" ") }
                .map { ChatColor.RESET.toString() + ChatColor.GRAY + it }
        stack.itemMeta = meta
        return stack
    }

    fun holdingItemGuard(entity: Entity, f: (Player) -> Unit) {
        if (
            entity is Player &&
                (isItem(entity.inventory.itemInMainHand) || isItem(entity.inventory.itemInOffHand))
        ) {
            f(entity)
        }
    }

    fun inventoryItemGuard(entity: Entity, f: (Player) -> Unit) {
        if (entity is Player && entity.inventory.contents.any(::isItem)) {
            f(entity)
        }
    }

    fun hotbarItemGuard(entity: Entity, f: (Player) -> Unit) {
        if (
            entity is Player &&
                (0 until 9).mapNotNull { entity.inventory.getItem(it) }.any(::isItem)
        ) {
            f(entity)
        }
    }

    open fun rightClick(player: Player) {}
    open fun leftClick(player: Player) {}
    open fun drop(player: Player) {}

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val item = event.item
        if (item != null && isItem(item)) {
            when (event.action) {
                Action.LEFT_CLICK_BLOCK,
                Action.LEFT_CLICK_AIR -> {
                    leftClick(event.player)
                }
                Action.RIGHT_CLICK_BLOCK,
                Action.RIGHT_CLICK_AIR -> {
                    rightClick(event.player)
                }
                else -> {}
            }
        }
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        if (isItem(event.itemDrop.itemStack)) {
            event.isCancelled = true
            drop(event.player)
        }
    }
}
