package heroes.ability

abstract class BasicAbility : Ability() {
    override fun use() {
        ability()
    }

    abstract fun ability()

    override fun tick() {}
}
