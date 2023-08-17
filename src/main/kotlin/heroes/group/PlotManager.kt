import java.util.UUID
import org.bukkit.Location

object PlotManager {
    data class Plot(val x1: Int, val z1: Int, val x2: Int, val z2: Int)

    val plots: MutableMap<UUID, MutableList<Plot>> = mutableMapOf()

    fun regionOwner(position: Location): UUID? =
            plots.entries
                    .find { (_, plots) ->
                        plots.any { plot ->
                            println("plot")
                            println(plot.x1)
                            println(plot.z1)
                            println(plot.x2)
                            println(plot.z2)
                            plot.x1 <= position.getX() &&
                                    position.getX() <= plot.x2 &&
                                    plot.z1 <= position.getZ() &&
                                    position.getZ() <= plot.z2
                        }
                    }
                    ?.key
}
