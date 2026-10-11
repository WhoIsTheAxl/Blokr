package de.bixilon.minosoft.data.world.portable

/**
 * Decodes padded 64-bit palette-packed block indices into a 4096-entry section.
 * This is a protocol-independent primitive, not a complete Minecraft chunk packet decoder.
 * Each long contains floor(64 / bitsPerEntry) entries; entries never cross long boundaries.
 */
object PalettedSectionDecoder {
    const val BLOCK_COUNT = 4096

    fun decode(bitsPerEntry: Int, palette: IntArray, packed: LongArray): IntArray {
        require(bitsPerEntry in 0..32) { "Bits per entry outside 0..32" }
        require(palette.isNotEmpty()) { "Palette must not be empty" }
        require(palette.all { it >= 0 }) { "Negative block state ID" }
        if (bitsPerEntry == 0) {
            require(palette.size == 1 && packed.isEmpty()) {
                "Single-value palette requires one state and no packed longs"
            }
            return IntArray(BLOCK_COUNT) { palette[0] }
        }
        require(palette.size.toLong() <= (1L shl bitsPerEntry)) {
            "Palette does not fit bits per entry"
        }
        val perLong = 64 / bitsPerEntry
        val expected = (BLOCK_COUNT + perLong - 1) / perLong
        require(packed.size == expected) { "Expected $expected packed longs, got ${packed.size}" }
        val mask = (1L shl bitsPerEntry) - 1L
        return IntArray(BLOCK_COUNT) { index ->
            val word = packed[index / perLong]
            val paletteIndex = ((word ushr ((index % perLong) * bitsPerEntry)) and mask).toInt()
            require(paletteIndex in palette.indices) { "Palette index out of bounds at block $index" }
            palette[paletteIndex]
        }
    }

    /** Packs local palette indices for tests and offline chunk staging. */
    fun encode(bitsPerEntry: Int, indices: IntArray): LongArray {
        require(bitsPerEntry in 1..32) { "Bits per entry outside 1..32" }
        require(indices.size == BLOCK_COUNT) { "Expected 4096 indices" }
        val perLong = 64 / bitsPerEntry
        val result = LongArray((BLOCK_COUNT + perLong - 1) / perLong)
        val limit = 1L shl bitsPerEntry
        indices.forEachIndexed { index, value ->
            require(value >= 0 && value.toLong() < limit) { "Index does not fit bits per entry" }
            val word = index / perLong
            result[word] = result[word] or (value.toLong() shl ((index % perLong) * bitsPerEntry))
        }
        return result
    }
}
