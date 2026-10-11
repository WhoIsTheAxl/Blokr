package de.bixilon.minosoft.data.world.portable

/** Coordinate conversions shared by protocol decoding, chunk storage and future meshing. */
object WorldCoordinates {
    const val SECTION_EDGE = 16
    const val SECTION_VOLUME = 4096

    fun section(value: Int): Int = Math.floorDiv(value, SECTION_EDGE)
    fun local(value: Int): Int = Math.floorMod(value, SECTION_EDGE)

    /** YZX layout: x varies fastest, then z, then y. */
    fun index(localX: Int, localY: Int, localZ: Int): Int {
        require(localX in 0..15 && localY in 0..15 && localZ in 0..15) {
            "Local block coordinates must be in 0..15"
        }
        return (localY shl 8) or (localZ shl 4) or localX
    }

    fun localX(index: Int): Int { requireIndex(index); return index and 15 }
    fun localY(index: Int): Int { requireIndex(index); return index ushr 8 }
    fun localZ(index: Int): Int { requireIndex(index); return (index ushr 4) and 15 }

    private fun requireIndex(index: Int) {
        require(index in 0 until SECTION_VOLUME) { "Section index outside 0..4095" }
    }
}
