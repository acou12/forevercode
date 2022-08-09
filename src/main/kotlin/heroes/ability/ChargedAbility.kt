package heroes.ability

import org.bukkit.entity.Player
import util.ChatUtil.sendGameMessage
import util.Timer

abstract class ChargedAbility(
    val chargeDelay: Long,
    val maxCharges: Long,
    val name: String,
    val player: Player,
) : Ability() {
    var charges = maxCharges
    val timer = Timer(chargeDelay)

    override fun use() {
        if (charges > 0) {
            charges--
            ability()
            player.sendGameMessage("${name} charges: ", charges)
        } else {
            player.sendGameMessage("You do not have any ", name, " charges.")
        }
    }

    override fun tick() {
        if (charges < maxCharges && timer.done()) {
            timer.reset()
            charges++
            player.sendGameMessage("${name} charges: ", charges)
        }
        if (charges == maxCharges) {
            timer.reset()
        }
    }

    abstract fun ability()
}
