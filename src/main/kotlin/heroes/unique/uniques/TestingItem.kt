package heroes.unique.uniques

import heroes.unique.Unique
import heroes.unique.UniqueUtil
import kotlin.random.Random
import org.bukkit.Material
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import util.SchedulerUtil

object TestingItem : Unique() {
    override val name: String = "Testing Item"
    override val lore: String =
        "For testing only; please do not right click, or serious bodily harm may occur."
    override val type: Material = Material.NETHER_STAR

    override fun rightClick(player: Player) {
        UniqueUtil.consume(player, this, UniqueUtil.ConsumptionRequirement.HAND_OR_OFFHAND)
        SchedulerUtil.delayedFor(1, 1..100) { i ->
            val creeperLocation =
                player.location.add(
                    Random.nextDouble(-5.0, 5.0),
                    10.0,
                    Random.nextDouble(-5.0, 5.0),
                )
            player.world.spawnEntity(creeperLocation, EntityType.CREEPER).let {
                it.isInvulnerable = true
                (it as Creeper).isPowered = true
            }
        }
    }
}
