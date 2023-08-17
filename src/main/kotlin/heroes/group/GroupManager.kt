import org.bukkit.entity.Player

object GroupManager {
    val groups: MutableList<Group> = mutableListOf()
    fun groupOfPlayer(player: Player): Group? = groups.find { it.members.contains(player.uniqueId) }
}
