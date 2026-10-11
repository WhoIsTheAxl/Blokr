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

    /**
     * Returns a defensive copy of one 16³ section, indexed as (y << 8) | (z << 4) | x.
     * Zero represents air. Missing sections return an all-air array.
     */
    @Synchronized
    fun sectionSnapshot(sectionX: Int, sectionY: Int, sectionZ: Int): IntArray {
        val result = IntArray(ChunkSize.BLOCKS_PER_SECTION)
        sections[SectionKey(sectionX, sectionY, sectionZ)]?.forEach { (index, stateId) ->
            result[index] = stateId
        }
        return result
    }

    /**
     * Atomically replace a section with a defensive copy of the supplied state IDs.
     * An all-air section is removed rather than retained in memory.
     */
    @Synchronized
    fun replaceSection(sectionX: Int, sectionY: Int, sectionZ: Int, states: IntArray) {
        require(states.size == ChunkSize.BLOCKS_PER_SECTION) { "Expected 4096 block states" }
        require(sectionY >= Math.floorDiv(ChunkSize.CHUNK_MIN_Y, 16)
            && sectionY < Math.floorDiv(ChunkSize.CHUNK_MAX_Y, 16)) { "Section Y outside world range" }
        require(states.all { it >= 0 }) { "Block state IDs must be nonnegative" }
        val key = SectionKey(sectionX, sectionY, sectionZ)
        val nonAir = HashMap<Int, Int>()
        states.forEachIndexed { index, stateId ->
            if (stateId != 0) nonAir[index] = stateId
        }
        if (nonAir.isEmpty()) sections.remove(key) else sections[key] = nonAir
    }

    /** Section coordinates currently containing non-air blocks; returned as a snapshot. */
    data class SectionPosition(val x: Int, val y: Int, val z: Int)

    /** Returns a stable snapshot; no internal mutable collections escape. */
    @Synchronized
    fun sectionPositions(): Set<SectionPosition> =
        sections.keys.mapTo(LinkedHashSet()) { SectionPosition(it.x, it.y, it.z) }

    /** Removes all sections in a horizontal chunk column, including negative coordinates. */
    @Synchronized
    fun unloadChunk(chunkX: Int, chunkZ: Int): Int {
        val keys = sections.keys.filter { it.x == chunkX && it.z == chunkZ }
        keys.forEach { sections.remove(it) }
        return keys.size
    }

    /** Returns a snapshot of the populated section Y coordinates in a chunk column. */
    @Synchronized
    fun chunkSectionYs(chunkX: Int, chunkZ: Int): Set<Int> =
        sections.keys.filter { it.x == chunkX && it.z == chunkZ }.mapTo(LinkedHashSet()) { it.y }

    /** Atomically replaces a complete chunk column. Missing section Ys become air. */
    @Synchronized
    fun replaceChunk(chunkX: Int, chunkZ: Int, sectionStates: Map<Int, IntArray>) {
        val minSection = Math.floorDiv(ChunkSize.CHUNK_MIN_Y, 16)
        val maxSection = Math.floorDiv(ChunkSize.CHUNK_MAX_Y - 1, 16)
        val replacements = HashMap<SectionKey, MutableMap<Int, Int>>()
        for ((sectionY, states) in sectionStates) {
            require(sectionY in minSection..maxSection) { "Section Y outside world range" }
            require(states.size == ChunkSize.BLOCKS_PER_SECTION) { "Expected 4096 block states" }
            val nonAir = HashMap<Int, Int>()
            states.forEachIndexed { index, stateId ->
                require(stateId >= 0) { "Block state IDs must be nonnegative" }
                if (stateId != 0) nonAir[index] = stateId
            }
            if (nonAir.isNotEmpty()) replacements[SectionKey(chunkX, sectionY, chunkZ)] = nonAir
        }
        unloadChunk(chunkX, chunkZ)
        sections.putAll(replacements)
    }

    /** Immutable update request. Later updates to the same block win. */
    data class BlockUpdate(val x: Int, val y: Int, val z: Int, val stateId: Int)

    /**
     * Applies an ordered group of block updates under one lock. All inputs are
     * validated before the first mutation, so invalid batches leave the world unchanged.
     */
    @Synchronized
    fun applyBlockUpdates(updates: Collection<BlockUpdate>) {
        updates.forEach {
            require(it.y >= ChunkSize.CHUNK_MIN_Y && it.y < ChunkSize.CHUNK_MAX_Y) {
                "Y outside supported chunk range"
            }
            require(it.stateId >= 0) { "Block state ID must be nonnegative" }
        }
        updates.forEach { setBlock(it.x, it.y, it.z, it.stateId) }
    }

    /** Defensive snapshot of populated sections in one chunk column. */
    @Synchronized
    fun chunkSnapshot(chunkX: Int, chunkZ: Int): Map<Int, IntArray> {
        val result = LinkedHashMap<Int, IntArray>()
        for (sectionY in chunkSectionYs(chunkX, chunkZ).sorted()) {
            result[sectionY] = sectionSnapshot(chunkX, sectionY, chunkZ)
        }
        return result
    }

    /** Number of chunk columns containing at least one non-air block. */
    @Synchronized
    fun loadedChunks(): Int =
        sections.keys.mapTo(HashSet()) { Pair(it.x, it.z) }.size

    /** Returns all populated chunk columns without exposing mutable world state. */
    @Synchronized
    fun chunkPositions(): Set<ChunkPosition> =
        sections.keys.mapTo(LinkedHashSet()) { ChunkPosition(it.x, it.z) }

    data class ChunkPosition(val x: Int, val z: Int)

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
