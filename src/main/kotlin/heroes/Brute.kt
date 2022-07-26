package heroes

import heroes.ability.TimedAbility
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class Brute(player: Player) : Hero() {
    val smashAbility =
        TimedAbility(5_000, "Smash", player) {
            player.world.entities
                .filterIsInstance<LivingEntity>()
                .filter { it != player && it.location.distance(player.location) < 20 }
                .forEach {
                    it.velocity =
                        it.location.subtract(player.location).toVector().normalize().setY(5.0)
                    it.damage(5.0)
                }
        }

    val hopAbility =
        TimedAbility(5_000, "Hop", player) {
            player.velocity = player.velocity.setY(3.0)
            //            SchedulerUtil.delayedFor(5, 1..20) { i ->
            //                val position =
            // player.location.add(player.location.direction.multiply(i.toDouble()))
            //                for (dx in -1..1) for (dy in -1..1) for (dz in -1..1) {
            //                    position.block.getRelative(dx, dy, dz).type = Material.AIR
            //                    position.world?.spawnParticle(Particle.EXPLOSION_LARGE, position,
            // 1)
            //                }
            //            }
        }

    override fun rightClick() {
        hopAbility.use()
    }

    override fun drop() {
        smashAbility.use()
    }

    override fun tick() {
        smashAbility.tick()
        hopAbility.tick()
    }

    override fun bow() {}
}
