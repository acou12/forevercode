package heroes

import heroes.ability.TimedAbility
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import util.ChatUtil
import util.Timer

class Oracle(player: Player) : Hero(player) {
    lateinit var tether: Location
    var tethered = false
    val tetherTickTimer = Timer(500)

    val setTetherAbility =
        TimedAbility(30_000, "Tether", player) {
            tether = player.location
            tethered = true
        }

    override fun rightClick() {
        if (tethered) {
            tethered = false
            player.teleport(tether)
            player.sendMessage(
                ChatUtil.gameMessage("You teleported to your ", "Tether", " location.")
            )
        } else {
            player.sendMessage(ChatUtil.gameMessage("You have not set a ", "Tether", " location."))
        }
    }

    override fun drop() {
        setTetherAbility.use()
    }

    override fun tick() {
        setTetherAbility.tick()
        if (tetherTickTimer.done()) {
            tetherTickTimer.reset()
            val headLocation = player.location.add(0.0, 1.0, 0.0)
            val delta = headLocation.subtract(tether)
            for (i in 0..10) {
                player.world.spawnParticle(
                    Particle.CRIT,
                    headLocation.add(delta.clone().multiply(i / 10.0)),
                    1
                )
            }
        }
    }

    override fun bow() {}

    override val type: HeroType = HeroType.ORACLE
}
