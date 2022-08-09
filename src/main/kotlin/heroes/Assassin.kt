package heroes

import heroes.ability.*
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Arrow
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import util.ChatUtil
import util.ChatUtil.sendGameMessage
import util.Util

class Assassin(player: Player) : Hero(player) {

    override val rightClickSword: Ability =
        object : TimedAbility(1_000, "Knock Out", player) {
            override fun ability() {
                val victim =
                    player
                        .getNearbyEntities(3.0, 3.0, 3.0)
                        .filterIsInstance<LivingEntity>()
                        .filter {
                            it.location.distance(player.location) < 3
                        } // TODO: extract into extension function
                        .map {
                            Pair(
                                it,
                                it.location
                                    .subtract(player.location)
                                    .toVector()
                                    .normalize()
                                    .dot(player.location.direction),
                            )
                        }
                        .filter { (_, dot) -> dot > 0.5 }
                        .maxByOrNull { (_, dot) -> dot }
                        ?.component1()
                if (victim == null) {
                    player.sendMessage(ChatUtil.gameMessage("You missed."))
                } else {
                    victim.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 20 * 3, 1))
                    victim.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 0))
                    victim.damage(4.0)
                }
            }
        }

    override val rightClickAxe: Ability =
        object : ChargedAbility(1_000, 20, "Flash", player) {
            val NUM_FLASH_PARTICLES = 10

            override fun ability() {
                val originalLocation = player.location.clone()
                player.teleport(player.location.add(player.location.direction.multiply(5)))
                Util.intermediateSteps(originalLocation, player.location, NUM_FLASH_PARTICLES)
                    .forEach { player.spawnParticle(Particle.SCRAPE, it, 1) }
            }
        }

    override val dropSword: Ability =
        object : TimedAbility(5_000, "Vanish", player) {
            override fun ability() {
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
                fun smokeBomb(location: Location) {
                    for (i in 1..10) {
                        location.world!!.spawnParticle(
                            Particle.SMOKE_LARGE,
                            location.add(Vector.getRandom().multiply(3.0)),
                            5
                        )
                    }
                }
                smokeBomb(player.location)
                smokeBomb(newLocation)
                player.teleport(newLocation)
            }
        }

    override val dropAxe: Ability = EmptyAbility

    override val bow: Ability =
        object : PrepareBowAbility(5_000, "Pin Down", player) {
            override fun onHitEntity(entity: Entity) {
                if (entity is LivingEntity) {
                    entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 3 * 20, 100))
                }
            }

            override fun onHitBlock(location: Location) {}
        }

    override fun tick() {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 1, 0))
    }

    @EventHandler
    fun onFall(event: EntityDamageEvent) =
        heroGuard(event.entity) { player ->
            if (event.cause == EntityDamageEvent.DamageCause.FALL) {
                event.isCancelled = true
                player.sendGameMessage("You used ", "Roll", ".")
            }
        }

    @EventHandler
    fun onArrowHit(event: EntityDamageByEntityEvent) =
        heroGuard(event.entity) { player ->
            if (event.damager is Arrow && Math.random() < 0.25) {
                player.sendGameMessage("You used", "Dodge", ".")
            }
        }

    override val type = HeroType.ASSASSINATOR
}
