import heroes.Hero
import heroes.HeroType
import heroes.ability.PrepareManager
import heroes.unique.UniqueManager
import kotlin.math.roundToInt
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
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import util.ChatUtil

class Main : JavaPlugin() {

    companion object {
        val playerHeroMap: MutableMap<Player, Hero> = mutableMapOf()
        data class ParticlePosition(val position: Location, val direction: Vector)
        lateinit var plugin: JavaPlugin
    }

    override fun onEnable() {
        plugin = this
        Bukkit.getScheduler()
                .scheduleSyncRepeatingTask(
                        this,
                        {
                            playerHeroMap.values.forEach { h ->
                                h.tick()
                                h.rightClickSword.tick()
                                h.rightClickAxe.tick()
                                h.dropSword.tick()
                                h.dropAxe.tick()
                                h.bow.tick()
                            }
                        },
                        1,
                        1
                )
        UniqueManager.init(this)
        this.server.pluginManager.registerEvents(
                object : Listener {
                    @EventHandler
                    fun onInteract(event: PlayerInteractEvent) {
                        // TODO: make this less boilerplatey and add
                        //       support for more complex combinations
                        val hero = playerHeroMap[event.player]
                        if (hero != null) {
                            when (event.action) {
                                Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR -> {
                                    when (event.item?.type) {
                                        Material.IRON_SWORD, Material.DIAMOND_SWORD -> {
                                            hero.rightClickSword.use()
                                        }
                                        Material.IRON_AXE, Material.DIAMOND_AXE -> {
                                            hero.rightClickAxe.use()
                                        }
                                        else -> {}
                                    }
                                }
                                Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> {
                                    when (event.item?.type) {
                                        Material.BOW -> {
                                            hero.bow.use()
                                        }
                                        else -> {}
                                    }
                                }
                                else -> {}
                            }
                        }
                    }

                    @EventHandler
                    fun onDrop(event: PlayerDropItemEvent) {
                        val hero = playerHeroMap[event.player] ?: return
                        event.isCancelled = true // TODO: implement a way to drop items
                        when (event.itemDrop.itemStack.type) {
                            Material.IRON_SWORD, Material.DIAMOND_SWORD -> {
                                hero.dropSword.use()
                            }
                            Material.IRON_AXE, Material.DIAMOND_AXE -> {
                                hero.dropAxe.use()
                            }
                            else -> {}
                        }
                    }
                },
                this
        )
        this.server.pluginManager.registerEvents(PrepareManager, this)
        this.server.pluginManager.registerEvents(GroupEvents, this)
    }

    override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String>
    ): Boolean {
        when (command.name) {
            "equip" -> {
                val player = sender as? Player ?: return true
                if (args.isEmpty()) {
                    HeroType.values().forEach { player.sendMessage("- ${it.name}") }
                    return false
                }
                val heroType =
                        HeroType.values().find { it.heroName.lowercase() == args[0].lowercase() }
                if (heroType == null) {
                    player.sendMessage("That hero does not exist.")
                    return false
                }
                val hero = heroType.heroConstructor(player)
                server.pluginManager.registerEvents(hero, this)
                playerHeroMap[player] = hero
                fun colored(type: Material): ItemStack {
                    val item = ItemStack(type)
                    val meta = item.itemMeta as LeatherArmorMeta
                    meta.setColor(heroType.color)
                    item.itemMeta = meta
                    return item
                }
                player.inventory.helmet = colored(Material.LEATHER_HELMET)
                player.inventory.chestplate = colored(Material.LEATHER_CHESTPLATE)
                player.inventory.leggings = colored(Material.LEATHER_LEGGINGS)
                player.inventory.boots = colored(Material.LEATHER_BOOTS)
                listOf(Material.IRON_AXE, Material.IRON_SWORD, Material.BOW).forEach {
                    if (!player.inventory.contains(it)) player.inventory.addItem(ItemStack(it))
                }
                player.inventory.remove(Material.ARROW)
                player.inventory.addItem(ItemStack(Material.ARROW, 64))
            }
            "particleemitters" -> {
                val player = sender as? Player ?: return true
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
                                    { player.world.spawnParticle(particles[i], location, 5) },
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
            "uniques" -> {
                val player = sender as? Player ?: return true
                UniqueManager.uniques.forEach { player.inventory.addItem(it.createItemStack()) }
            }
            "group" -> {
                val player = sender as? Player ?: return true
                if (args.size == 0) {
                    player.sendMessage(ChatUtil.gameMessage("Invalid command usage."))
                    return false
                }
                when (args[0]) {
                    "add" -> {
                        if (args.size == 1) {
                            player.sendMessage(ChatUtil.gameMessage("Invalid command usage."))
                            return false
                        }
                        val groupName = args.drop(1).joinToString(" ")
                        GroupManager.groups.add(Group(groupName))
                        player.sendMessage(ChatUtil.gameMessage("You created ", groupName, "."))
                    }
                    "join" -> {
                        if (args.size == 1) {
                            player.sendMessage(ChatUtil.gameMessage("Invalid command usage."))
                            return false
                        }
                        val groupName = args.drop(1).joinToString(" ")
                        val group = GroupManager.groups.find { it.name == groupName }
                        if (group == null) {
                            player.sendMessage(
                                    ChatUtil.gameMessage(
                                            "The group ",
                                            groupName,
                                            " does not exist."
                                    )
                            )
                            return false
                        }
                        group.members.add(player.uniqueId)
                    }
                    "list" -> {
                        player.sendMessage(
                                GroupManager.groups.joinToString("\n") {
                                    it.name + " - " + it.members.joinToString(", ")
                                }
                        )
                    }
                    "claim" -> {
                        if (args.size < 3) {
                            player.sendMessage(ChatUtil.gameMessage("Invalid command usage."))
                            return false
                        }
                        val group = GroupManager.groupOfPlayer(player)
                        if (group == null) {
                            player.sendMessage(ChatUtil.gameMessage("You are not part of a group."))
                            return false
                        }
                        val claimWidth = args[1].toInt()
                        val claimHeight = args[2].toInt()
                        val plot =
                                PlotManager.Plot(
                                        player.location.getX().roundToInt() - claimWidth,
                                        player.location.getY().roundToInt() - claimHeight,
                                        player.location.getX().roundToInt() + claimWidth,
                                        player.location.getY().roundToInt() + claimHeight,
                                )
                        PlotManager.plots.getOrPut(group.uuid, { mutableListOf() }).add(plot)
                    }
                }
            }
        }
        return true
    }
}
