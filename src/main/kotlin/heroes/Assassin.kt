package heroes

import heroes.ability.ChargedAbility
import heroes.ability.PrepareBowAbility
import heroes.ability.TimedAbility
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class Assassin(player: Player) : Hero() {
    val teleportAbility =
        ChargedAbility(1_000, 20, "Flash", player) {
            val originalLocation = player.location.clone()
            player.teleport(player.location.add(player.location.direction.multiply(5)))
            val NUM_FLASH_PARTICLES = 10
            val delta =
                player.location.subtract(originalLocation).multiply(1.0 / NUM_FLASH_PARTICLES)
            for (i in 0 until NUM_FLASH_PARTICLES) {
                player.spawnParticle(
                    Particle.SPELL_MOB,
                    player.location.add(delta.multiply(i.toDouble())),
                    1
                )
            }
        }

    val vanishAbility =
        TimedAbility(5_000, "Vanish", player) {
            val radius = Random.nextInt(20, 30)
            val angle = Random.nextDouble(Math.PI * 2)
            val newLocation =
                player.location.add(
                    round(radius * cos(angle)),
                    1.0,
                    round(radius * sin(angle)),
                )
            for (y in 255 downTo 0) {
                if (
                    !player.world
                        .getBlockAt(newLocation.x.roundToInt(), y, newLocation.z.roundToInt())
                        .type
                        .isAir
                ) {
                    newLocation.y = y.toDouble()
                    break
                }
            }
            for (i in 1..30) {
                player.world.spawnParticle(
                    Particle.SMOKE_LARGE,
                    player.location.add(Vector.getRandom().multiply(3.0)),
                    5
                )
            }
            player.teleport(newLocation)
        }

    val pinDownAbility =
        PrepareBowAbility(
            5_000,
            "Pin Down",
            player,
            { e ->
                if (e is LivingEntity) {
                    e.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 3 * 20, 100))
                }
            },
            {}
        )

    override fun rightClick() {
        teleportAbility.use()
    }

    override fun drop() {
        vanishAbility.use()
    }

    override fun tick() {
        teleportAbility.tick()
        vanishAbility.tick()
        pinDownAbility.tick()
    }

    override fun bow() {
        pinDownAbility.use()
    }
}
