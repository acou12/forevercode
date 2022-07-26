package heroes.ability

import org.bukkit.entity.Player
import util.ChatUtil

class ChargedAbility(
    val chargeDelay: Long,
    val maxCharges: Long,
    val name: String,
    val player: Player,
    val ability: () -> Unit
) : Ability() {
    var charges = maxCharges
    var lastCharge = 0L

    override fun use() {
        if (charges > 0) {
            charges--
            ability()
            player.sendMessage(
                ChatUtil.gameMessage("You used ", name, ". You have ", charges, " more charges.")
            )
        } else {
            player.sendMessage(ChatUtil.gameMessage("You do not have any ", name, " charges."))
        }
    }

    override fun tick() {
        val currentTime = System.currentTimeMillis()
        if (charges < maxCharges && currentTime - lastCharge > chargeDelay) {
            charges++
            lastCharge = currentTime
            player.sendMessage(ChatUtil.gameMessage("You have ", "${charges} ${name}", " charges."))
        }
    }
}
