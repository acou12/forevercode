package heroes

import heroes.ability.Ability
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Listener

abstract class Hero(val player: Player) : Listener {
    abstract val rightClickSword: Ability
    abstract val rightClickAxe: Ability
    abstract val dropSword: Ability
    abstract val dropAxe: Ability
    abstract val bow: Ability
    abstract fun tick()
    abstract val type: HeroType
    fun heroGuard(entity: Entity, f: (Player) -> Unit) {
        if (entity is Player && Main.playerHeroMap[entity]?.type == type) f(entity)
    }
}
