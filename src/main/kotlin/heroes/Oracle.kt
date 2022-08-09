package heroes

import heroes.ability.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import util.ChatUtil
import util.ChatUtil.sendGameMessage
import util.Timer
import util.Util

class Oracle(player: Player) : Hero(player) {
    companion object {
        const val MAX_TETHER_RANGE = 50
    }

    lateinit var tether: Location
    var tethered = false
    val tetherTickTimer = Timer(1_000).finish()

    override val rightClickSword: Ability =
        object : BasicAbility() {
            override fun ability() {
                if (tethered) {
                    tethered = false
                    player.fallDistance = 0.0f
                    player.teleport(tether)
                    player.sendMessage(
                        ChatUtil.gameMessage("You teleported to your ", "Tether", " location.")
                    )
                } else {
                    player.sendGameMessage("You have not set a ", "Tether", " location.")
                }
            }
        }

    override val rightClickAxe: Ability = EmptyAbility

    override val dropSword: Ability =
        object : TimedAbility(1_000, "Tether", player) {
            override fun ability() {
                tether = player.location
                tethered = true
            }
        }

    override val dropAxe: Ability = EmptyAbility

    override val bow: Ability =
        object : PrepareBowAbility(1_000, "Arrow of Vision", player) {
            override fun onHitEntity(entity: Entity) {
                val hitPlayer = entity as? Player ?: return
                val inventory =
                    Bukkit.createInventory(
                        null,
                        InventoryType.PLAYER,
                        "${hitPlayer.displayName}'s inventory"
                    )
                inventory.contents = hitPlayer.inventory.contents.clone()
                readonlyInventory = inventory
                player.openInventory(inventory)
            }

            override fun onHitBlock(location: Location) {
                val prettyName =
                    location.block.type.name.split("_").joinToString(separator = " ") { word ->
                        word.lowercase().replaceFirstChar { c -> c.uppercase() }
                    }
                player.sendGameMessage(
                    "You missed, and hit ${ChatUtil.aOrAn(prettyName)} ",
                    prettyName,
                    "."
                )
            }
        }

    override fun tick() {
        if (tethered && tetherTickTimer.done()) {
            tetherTickTimer.reset()
            val headLocation = player.location.add(0.0, 1.5, 0.0)
            Util.intermediateSteps(headLocation, tether, 20).forEach {
                player.world.spawnParticle(Particle.SCRAPE, it, 1)
            }
        }
        if (tethered && player.location.distance(tether) > MAX_TETHER_RANGE) {
            tethered = false
            player.sendMessage(
                ChatUtil.gameMessage(
                    "Your ",
                    "Tether",
                    " broke because you exceeded ${MAX_TETHER_RANGE} blocks."
                )
            )
            player.playSound(player.location, Sound.ENTITY_SPLASH_POTION_BREAK, 1f, 1f)
        }
    }

    var readonlyInventory: Inventory? = null

    @EventHandler
    fun inventoryInteract(event: InventoryInteractEvent) =
        heroGuard(event.whoClicked) {
            if (event.inventory === readonlyInventory) {
                event.isCancelled = true
            }
        }

    override val type: HeroType = HeroType.ORACLE
}
