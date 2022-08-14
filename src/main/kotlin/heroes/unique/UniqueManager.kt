package heroes.unique

import heroes.unique.uniques.AwesomeFeatherOfCoolness
import heroes.unique.uniques.Cookie
import heroes.unique.uniques.TestingItem
import org.bukkit.plugin.java.JavaPlugin

object UniqueManager {
    val uniques: List<Unique> = listOf(TestingItem, Cookie, AwesomeFeatherOfCoolness)

    fun init(plugin: JavaPlugin) {
        uniques.forEach { plugin.server.pluginManager.registerEvents(it, plugin) }
    }
}
