package util

import org.bukkit.ChatColor

object ChatUtil {
    fun gameMessage(vararg items: Any): String {
        return items
            .map { it.toString() }
            .mapIndexed { index, s ->
                (if (index % 2 == 0) ChatColor.GOLD else ChatColor.BLUE).toString() + s
            }
            .joinToString(separator = "")
    }
}
