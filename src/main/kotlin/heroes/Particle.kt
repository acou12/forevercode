package heroes

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import util.ChatUtil

class Particle(val player: Player) : Hero() {

    override fun rightClick() {
        player.sendMessage(ChatUtil.gameMessage("You prepared ", "Spacetime Rupture", "."))
    }

    override fun drop() {
        player.inventory.addItem(
            run {
                val sword = ItemStack(Material.IRON_SWORD)
                val meta = sword.itemMeta!!
                meta.setDisplayName(ChatColor.RESET.toString() + ChatColor.GOLD + "Particle Sword")
                sword.itemMeta = meta
                return@run sword
            }
        )
    }

    override fun tick() {}

    override fun bow() {}
}
