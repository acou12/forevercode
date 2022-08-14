package heroes.unique.uniques

import heroes.unique.StatefulUnique
import org.bukkit.Material
import org.bukkit.entity.Player
import util.ChatUtil.sendGameMessage

object Cookie : StatefulUnique<Cookie.CookieState>() {
    data class CookieState(var num: Int)

    override fun defaultState(player: Player): CookieState = CookieState(0)

    override val name: String = "The Cookie"
    override val lore: String = "Left click to Cookie."
    override val type: Material = Material.COOKIE

    override fun leftClick(player: Player) {
        val state = getState(player)
        state.num++
        val cookieAchievements: Map<Int, String> =
            mapOf(
                3 to "Few Cookie",
                5 to "Some Cookie",
                10 to "Pretty Cookie",
                20 to "Much Cookie",
                50 to "Very Cookie",
                100 to "Lot Cookie",
                200 to "Ton Cookie",
                500 to "Omega Cookie",
                1000 to "All Cookie",
            )
        player.sendGameMessage("You have ", "Cookie", "d ", state.num, " times.")
        val achievement = cookieAchievements[state.num]
        if (achievement != null) {
            player.sendGameMessage("You have unlocked the achievement ", achievement, ".")
        }
        if (state.num >= 1000) {
            player.damage(10000.0)
            player.sendGameMessage("You ate too many cookies and ", "died", " instantly.")
        }
    }
}
