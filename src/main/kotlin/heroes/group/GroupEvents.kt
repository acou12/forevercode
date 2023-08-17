import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import util.ChatUtil

object GroupEvents : Listener {
    @EventHandler
    fun blockBreak(event: BlockBreakEvent) {
        val player = event.player
        val group = GroupManager.groupOfPlayer(player)
        val breakGroupUuid = PlotManager.regionOwner(event.block.location)
        print("EVENT:")
        println(group)
        println(breakGroupUuid)
        if (breakGroupUuid != null) {
            if (group == null || breakGroupUuid !== group.uuid) {
                event.isCancelled = true
                val breakGroup = GroupManager.groups.find { it.uuid === breakGroupUuid }!!
                player.sendMessage(
                        ChatUtil.gameMessage(
                                "You can't break this block. It belongs to ",
                                breakGroup.name,
                                "."
                        )
                )
            }
        }
    }
}
