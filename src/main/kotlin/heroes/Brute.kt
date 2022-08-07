package heroes

import heroes.ability.TimedAbility
import kotlin.math.floor
import kotlin.math.hypot
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import util.SchedulerUtil

class Brute(player: Player) : Hero(player) {
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
            hopped = true
        }

    var hopped = false

    override fun rightClick() {
        hopAbility.use()
    }

    override fun drop() {
        smashAbility.use()
    }

    override fun tick() {
        smashAbility.tick()
        hopAbility.tick()
        if (hopped && !player.location.clone().add(0.0, -0.1, 0.0).block.type.isAir) {
            hopped = false
            player.fallDistance = 0.0f
            val RADIUS = 20
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
            SchedulerUtil.delayedFor(2, 2 until RADIUS) {
                blockPartition[it]?.forEach { (x, z) ->
                    for (y in -3..3) {
                        val block = location.world!!.getBlockAt(x, y, z)
                        val data = block.blockData
                        val type = block.type
                        block.type = Material.AIR
                        if (!type.isAir) {
                            val falling = location.world!!.spawnFallingBlock(block.location, data)
                            falling.velocity = Vector.getRandom().setY(1)
                        }
                    }
                }
            }
        }
    }

    override fun bow() {}

    override val type = HeroType.BRUTE
}
