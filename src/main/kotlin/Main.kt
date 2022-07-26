import heroes.Hero
import heroes.HeroType
import heroes.ability.PrepareManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Sign
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import util.SchedulerUtil

class Main : JavaPlugin() {

    companion object {
        val playerHeroMap: MutableMap<Player, Hero> = mutableMapOf()
        data class ParticlePosition(val position: Location, val direction: Vector)
        val particleList: MutableList<ParticlePosition> = mutableListOf()
        lateinit var plugin: JavaPlugin
    }

    override fun onEnable() {
        plugin = this
        Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(
                this,
                {
                    playerHeroMap.values.forEach(Hero::tick)
                    playerHeroMap.keys.forEach { player ->
                        if (player.isSneaking) {
                            val headLocation = player.location.add(0.0, 1.2, 0.0)
                            val particleLocation = headLocation.add(player.location.direction)
                            player.spawnParticle(Particle.DRIP_LAVA, particleLocation, 1)
                            particleList.add(
                                ParticlePosition(
                                    headLocation.clone(),
                                    player.location.direction.clone()
                                )
                            )
                        }
                    }
                },
                1,
                1
            )
        this.server.pluginManager.registerEvents(
            object : Listener {
                @EventHandler
                fun onInteract(event: PlayerInteractEvent) {
                    val hero = playerHeroMap[event.player]
                    if (hero != null) {
                        if (
                            (event.action == Action.RIGHT_CLICK_AIR ||
                                event.action == Action.RIGHT_CLICK_BLOCK) &&
                                event.item != null &&
                                event.item!!.type in
                                    listOf(
                                        Material.IRON_AXE,
                                        Material.IRON_SWORD,
                                    )
                        ) {
                            hero.rightClick()
                        } else if (
                            (event.action == Action.LEFT_CLICK_AIR ||
                                event.action == Action.LEFT_CLICK_BLOCK) &&
                                event.item != null &&
                                event.item!!.type in listOf(Material.BOW, Material.CROSSBOW)
                        ) {
                            hero.bow()
                        }
                    }
                }

                @EventHandler
                fun onDrop(event: PlayerDropItemEvent) {
                    val hero = playerHeroMap[event.player] ?: return
                    event.isCancelled = true
                    hero.drop()
                }

                @EventHandler
                fun shift(event: PlayerToggleSneakEvent) {
                    if (!event.isSneaking) {
                        val copyList = particleList.map(ParticlePosition::copy)
                        particleList.clear()
                        Bukkit.getScheduler()
                            .scheduleSyncDelayedTask(
                                this@Main,
                                {
                                    SchedulerUtil.delayedFor(1, copyList.indices) {
                                        val (particlePosition, dir) = copyList[it]
                                        SchedulerUtil.delayedFor(1, 1..20) { i ->
                                            val position =
                                                particlePosition
                                                    .clone()
                                                    .add(dir.clone().multiply(i.toDouble()))
                                            for (dx in -1..1) for (dy in -1..1) for (dz in -1..1) {
                                                position.block.getRelative(dx, dy, dz).type =
                                                    Material.AIR

                                                position.world?.spawnParticle(
                                                    Particle.EXPLOSION_LARGE,
                                                    position,
                                                    1
                                                )
                                            }
                                        }
                                    }
                                },
                                40
                            )
                    }
                }
            },
            this
        )
        this.server.pluginManager.registerEvents(PrepareManager, this)
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        when (command.name) {
            "equip" -> {
                val player = sender as? Player ?: return false
                if (args.isEmpty()) {
                    HeroType.values().forEach { player.sendMessage("- ${it.name}") }
                    return false
                }
                val hero = HeroType.values().find { it.heroName.lowercase() == args[0].lowercase() }
                if (hero == null) {
                    player.sendMessage("That hero does not exist.")
                    return false
                }
                playerHeroMap[player] = hero.heroConstructor(player)
                player.inventory.helmet = run {
                    val helmet = ItemStack(Material.LEATHER_HELMET)
                    val meta = helmet.itemMeta as LeatherArmorMeta
                    meta.setColor(hero.color)
                    helmet.itemMeta = meta
                    helmet
                }
                player.inventory.chestplate = ItemStack(Material.IRON_CHESTPLATE)
                player.inventory.leggings = ItemStack(Material.IRON_LEGGINGS)
                player.inventory.boots = ItemStack(Material.IRON_BOOTS)
                listOf(Material.IRON_AXE, Material.IRON_SWORD).forEach {
                    if (!player.inventory.contains(it)) player.inventory.addItem(ItemStack(it))
                }
            }
            "particleemitters" -> {
                val player = sender as? Player ?: return false
                val particles = Particle.values()
                val rows = 10
                val cols = particles.size / rows + 1
                for (r in 0 until rows) for (c in 0 until cols) {
                    val i = r * cols + c
                    if (i >= particles.size) break
                    val location = player.location.add(c * 6.0, r * 6.0, 0.0)
                    Bukkit.getScheduler()
                        .scheduleSyncRepeatingTask(
                            this,
                            { player.spawnParticle(particles[i], location, 5) },
                            r * 20L,
                            100L,
                        )
                    val block = player.world.getBlockAt(location.add(0.0, -1.0, 0.0))
                    block.type = Material.ACACIA_SIGN
                    val state = block.state as Sign
                    state.setLine(0, particles[i].name)
                    state.update()
                }
            }
        }
        return true
    }
}
