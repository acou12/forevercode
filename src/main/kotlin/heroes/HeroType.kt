package heroes

import org.bukkit.Color
import org.bukkit.entity.Player

enum class HeroType(val heroName: String, val color: Color, val heroConstructor: (Player) -> Hero) {
    ASSASSINATOR("Assassinator", Color.fromRGB(0, 0, 0), ::Assassin),
    BRUTE("Brute", Color.fromRGB(255, 0, 0), ::Brute),
}
