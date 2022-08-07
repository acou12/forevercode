package heroes.ability

import org.bukkit.entity.Player
import util.ChatUtil
import util.ChatUtil.sendGameMessage
import util.Timer

class ChargedAbility(
    val chargeDelay: Long,
    val maxCharges: Long,
    val name: String,
    val player: Player,
    val ability: () -> Unit
) : Ability() {
    var charges = maxCharges
    val timer = Timer(chargeDelay)

    override fun use() {
        if (charges > 0) {
            charges--
            ability()
            player.sendMessage(
                ChatUtil.gameMessage("You used ", name, ". You have ", charges, " more charges.")
            )
        } else {
            player.sendGameMessage("You do not have any ", name, " charges.")
        }
    }

    override fun tick() {
        if (charges < maxCharges && timer.done()) {
            timer.reset()
            charges++
            player.sendGameMessage("You have ", "${charges} ${name}", " charges.")
        }
        if (charges == maxCharges) {
            timer.reset()
        }
    }
}
