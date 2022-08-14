package util

import org.bukkit.ChatColor
import org.bukkit.entity.Player

object ChatUtil {
    fun gameMessage(vararg items: Any): String {
        return items
            .map(Any::toString)
            .mapIndexed { index, s ->
                (if (index % 2 == 0) ChatColor.GRAY else ChatColor.LIGHT_PURPLE).toString() + s
            }
            .joinToString(separator = "")
    }

    // todo: hour?
    fun aOrAn(nextWord: String): String =
        if (nextWord[0].lowercase() in listOf("e", "a", "o", "u", "i")) "an" else "a"

    fun Player.sendGameMessage(vararg items: Any): Unit = this.sendMessage(gameMessage(*items))
}
