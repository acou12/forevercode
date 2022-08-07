package heroes

import heroes.ability.PrepareBowAbility
import heroes.ability.TimedAbility
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
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

    val setTetherAbility =
        TimedAbility(10_000, "Tether", player) {
            tether = player.location
            tethered = true
        }

    override fun rightClick() {
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

    override fun drop() {
        setTetherAbility.use()
    }

    override fun tick() {
        setTetherAbility.tick()
        if (tethered && tetherTickTimer.done()) {
            tetherTickTimer.reset()
            val headLocation = player.location.add(0.0, 1.0, 0.0)
            Util.intermediateSteps(headLocation, tether, 20).map {
                player.world.spawnParticle(Particle.FIREWORKS_SPARK, it, 1)
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

    val visionAbility =
        PrepareBowAbility(
            60_000,
            "Arrow of Vision",
            player,
            {
                if (it is Player) {
                    val inventory = Bukkit.createInventory(null, InventoryType.PLAYER, "Readonly")
                    inventory.contents = it.inventory.contents.clone()
                    readonlyInventory = inventory
                    player.openInventory(inventory)
                }
            },
            {}
        )

    @EventHandler
    fun inventoryInteract(event: InventoryInteractEvent) =
        heroGuard(event.whoClicked) {
            if (event.inventory === readonlyInventory) {
                event.isCancelled = true
            }
        }

    override fun bow() {
        visionAbility.use()
    }

    override val type: HeroType = HeroType.ORACLE
}
