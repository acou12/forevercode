import java.util.UUID

class Group(val name: String) {
    val uuid: UUID = UUID.randomUUID()
    val members: MutableList<UUID> = mutableListOf()
}
