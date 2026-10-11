package de.bixilon.minosoft.data.world.portable

/**
 * Small validated block-state ID catalog for the Android staging layer.
 * This is NOT the upstream Minosoft block registry or a Minecraft data pack loader.
 * ID zero is always air; unknown IDs remain unknown instead of being guessed.
 */
class BlockStateCatalog {
    data class Entry(val id: Int, val name: String)

    private val byId = LinkedHashMap<Int, Entry>()
    private val byName = HashMap<String, Entry>()

    init { register(0, "minecraft:air") }

    @Synchronized
    fun register(id: Int, name: String): Entry {
        require(id >= 0) { "Block state ID must be nonnegative" }
        require(name.isNotBlank() && ':' in name && !name.any { it.isWhitespace() }) {
            "Expected namespaced block identifier"
        }
        require(id != 0 || name == "minecraft:air") { "State zero is reserved for air" }
        require(name != "minecraft:air" || id == 0) { "Air must use state zero" }
        val existingId = byId[id]
        val existingName = byName[name]
        require(existingId == null || existingId.name == name) { "ID already registered" }
        require(existingName == null || existingName.id == id) { "Name already registered" }
        return existingId ?: Entry(id, name).also {
            byId[id] = it
            byName[name] = it
        }
    }

    @Synchronized fun findById(id: Int): Entry? = byId[id]
    @Synchronized fun findByName(name: String): Entry? = byName[name]
    @Synchronized fun entries(): List<Entry> = byId.values.toList()
    @Synchronized fun size(): Int = byId.size
}
