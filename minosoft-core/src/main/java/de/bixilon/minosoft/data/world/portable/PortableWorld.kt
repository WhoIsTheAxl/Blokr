package de.bixilon.minosoft.data.world.portable

import de.bixilon.minosoft.data.world.chunk.ChunkSize

/**
 * Android-safe world-state staging layer, NOT the upstream Minosoft World class.
 * Stores block state IDs in sparse sections until the upstream chunk manager,
 * block registries and PlaySession dependencies can be ported.
 * No rendering, persistence or network synchronization.
 */
class PortableWorld {
    data class Position(val x: Int, val y: Int, val z: Int)
    private data class SectionKey(val x: Int, val y: Int, val z: Int)

    private val sections = HashMap<SectionKey, MutableMap<Int, Int>>()
    var tickCount: Long = 0
        private set

    @Synchronized
    fun setBlock(x: Int, y: Int, z: Int, stateId: Int) {
        require(y >= ChunkSize.CHUNK_MIN_Y && y < ChunkSize.CHUNK_MAX_Y) { "Y outside supported chunk range" }
        require(stateId >= 0) { "Block state ID must be nonnegative" }
        val key = sectionKey(x, y, z)
        val local = localIndex(x, y, z)
        if (stateId == 0) {
            sections[key]?.let {
                it.remove(local)
                if (it.isEmpty()) sections.remove(key)
            }
        } else {
            sections.getOrPut(key) { HashMap() }[local] = stateId
        }
    }

    @Synchronized
    fun getBlock(x: Int, y: Int, z: Int): Int {
        if (y < ChunkSize.CHUNK_MIN_Y || y >= ChunkSize.CHUNK_MAX_Y) return 0
        return sections[sectionKey(x, y, z)]?.get(localIndex(x, y, z)) ?: 0
    }

    @Synchronized
    fun loadedSections(): Int = sections.size

    @Synchronized
    fun tick() { tickCount++ }

    @Synchronized
    fun clear() {
        sections.clear()
        tickCount = 0
    }

    private fun sectionKey(x: Int, y: Int, z: Int) =
        SectionKey(Math.floorDiv(x, 16), Math.floorDiv(y, 16), Math.floorDiv(z, 16))

    private fun localIndex(x: Int, y: Int, z: Int): Int =
        (Math.floorMod(y, 16) shl 8) or (Math.floorMod(z, 16) shl 4) or Math.floorMod(x, 16)
}
