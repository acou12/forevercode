package heroes

import heroes.ability.*
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import util.ChatUtil
import util.SchedulerUtil
import util.Timer
import util.Util

class Brute(player: Player) : Hero(player) {
    override val dropAxe: Ability =
        object : TimedAbility(5_000, "Smash", player) {
            override fun ability() {
                player.world.entities
                    .filterIsInstance<LivingEntity>()
                    .filter { it != player && it.location.distance(player.location) < 20 }
                    .forEach {
                        it.velocity =
                            it.location.subtract(player.location).toVector().normalize().setY(5.0)
                        it.world.spawnParticle(Particle.BLOCK_DUST, it.location, 5)
                        it.damage(5.0)
                    }
            }
        }

    override val rightClickAxe: Ability =
        object : TimedAbility(5_000, "Hop", player) {
            override fun ability() {
                player.velocity = player.velocity.setY(3.0)
                hopped = true
                hopDelay.reset()
            }
        }

    override val rightClickSword: Ability =
        object : BuiltAbility(5_000, 3_000, "Obliterate", player) {
            override fun buildStart() {}

            var pitch = 1f

            override fun buildTick() {
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, 1f, pitch)
                pitch += 0.5f
            }

            override fun buildEnd(buildTime: Long) {
                val numSteps = floor(buildTime / 100.toDouble()).toInt()
                SchedulerUtil.delayedFor(
                    1,
                    Util.intermediateSteps(
                        player.location,
                        player.location.add(player.location.direction.multiply(numSteps)),
                        numSteps
                    )
                ) { loc -> loc.world?.createExplosion(loc, 1f) }
            }
        }

    override val dropSword: Ability =
        object : TimedAbility(5_000, "Enclose", player) {
            override fun ability() {
                val location = player.location.clone()
                SchedulerUtil.delayedFor(2, -1..3) { y ->
                    for (x in -2..2) for (z in -2..2) {
                        if (x == -2 || x == 2 || z == -2 || z == 2) { // borders
                            location.block.getRelative(x, y, z).type = Material.COBBLESTONE
                        }
                    }
                }
            }
        }

    var hopped = false
    val hopDelay = Timer(2_000)

    override fun tick() {
        if (
            hopped &&
                hopDelay.done() &&
                !player.location.clone().add(0.0, -0.1, 0.0).block.type.isAir
        ) {
            hopped = false
            player.fallDistance = 0.0f
            val RADIUS = 5
            val location = player.location.clone()
            val blockPartition =
                (-RADIUS..RADIUS)
                    .flatMap { x -> (-RADIUS..RADIUS).map { y -> Pair(x, y) } }
                    .groupBy { (x, y) -> floor(hypot(x.toDouble(), y.toDouble())).toInt() }
                    .filterKeys { it <= RADIUS }
            val entityPartition =
                location.world!!
                    .getNearbyEntities(
                        location,
                        RADIUS.toDouble(),
                        RADIUS.toDouble(),
                        RADIUS.toDouble()
                    )
                    .filterIsInstance<LivingEntity>()
                    .groupBy { floor(it.location.distance(location)).toInt() }
                    .filterKeys { it <= RADIUS }
            SchedulerUtil.delayedFor(2, 2 until RADIUS) { i ->
                blockPartition[i]?.forEach { (x, z) ->
                    for (y in -3..3) {
                        val block = location.block.getRelative(x, y, z)
                        val data = block.blockData
                        val type = block.type
                        block.type = Material.AIR
                        if (!type.isAir) {
                            val falling = location.world!!.spawnFallingBlock(block.location, data)
                            falling.velocity = Vector(0.0, Random.nextDouble(1.0, 2.0), 0.0)
                        }
                    }
                }
            }
            SchedulerUtil.delayedFor(2, 0 until RADIUS) { i ->
                entityPartition[i]?.forEach { e ->
                    e.velocity =
                        e.location.subtract(player.location).toVector().normalize().setY(5.0)
                    e.damage(5.0)
                }
            }
        }
        if (shockedEntities.isNotEmpty() && shockTimer.done()) {
            shockTimer.reset()
            val beforeShockedEntities = shockedEntities
            shockedEntities =
                shockedEntities
                    .flatMap {
                        it.getNearbyEntities(6.0, 6.0, 6.0)
                            .filterIsInstance<LivingEntity>()
                            .filter { other -> other.location.distance(it.location) <= 6 }
                            .filter { e -> e !== it }
                    }
                    .distinct()
            shockedEntities.forEach { it.damage(1.0) }
            beforeShockedEntities.forEach { a ->
                shockedEntities.forEach { b ->
                    Util.intermediateSteps(a.location, b.location, 6).forEach {
                        a.world.spawnParticle(Particle.SPELL_INSTANT, it, 1)
                    }
                }
            }
        }
    }

    var shockedEntities: List<LivingEntity> = listOf()
    val shockTimer = Timer(500)

    override val bow =
        object : PrepareBowAbility(1_000, "Electric Arrow", player) {
            override fun onHitEntity(entity: Entity) {
                if (entity is LivingEntity) {
                    entity.sendMessage(
                        ChatUtil.gameMessage("You were hit by an ", "Electric Arrow", ".")
                    )
                    shockedEntities = listOf(entity)
                    shockTimer.reset()
                }
            }

            override fun onHitBlock(location: Location) {}
        }

    override val type = HeroType.BRUTE
}
