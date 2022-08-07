package heroes

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Listener

abstract class Hero(val player: Player) : Listener {
    abstract fun rightClick()
    abstract fun drop()
    abstract fun tick()
    abstract fun bow()
    abstract val type: HeroType
    fun heroGuard(entity: Entity, f: (Player) -> Unit) {
        if (entity is Player && Main.playerHeroTypeMap[entity as Player] == type) f(entity)
    }
}
