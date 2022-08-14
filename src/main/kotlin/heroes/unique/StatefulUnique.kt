package heroes.unique

import org.bukkit.entity.Player

abstract class StatefulUnique<State> : Unique() {
    val stateMap: MutableMap<Player, State> = mutableMapOf()

    abstract fun defaultState(player: Player): State

    fun getState(player: Player): State {
        return stateMap.getOrPut(player) { defaultState(player) }
    }
}
