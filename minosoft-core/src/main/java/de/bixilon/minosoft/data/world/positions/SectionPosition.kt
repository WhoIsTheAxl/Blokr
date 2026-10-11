/* Minosoft Copyright (C) 2020-2026 Moritz Zwerger
 * GPL-3.0-or-later. Adapted from upstream for Android without KMath/KUtil.
 */
package de.bixilon.minosoft.data.world.positions

/** Upstream Minosoft packed section coordinates: 22-bit X/Z, 8-bit Y. */
@JvmInline
value class SectionPosition(val raw: Long) {
    constructor(x: Int, y: SectionHeight, z: Int) : this(
        ((y.toLong() and MASK_Y.toLong()) shl SHIFT_Y) or
        ((z.toLong() and MASK_Z.toLong()) shl SHIFT_Z) or
        (x.toLong() and MASK_X.toLong())
    ) {
        require(x in -MAX_X..MAX_X && z in -MAX_Z..MAX_Z)
        require(y in MIN_Y..MAX_Y)
    }
    val x: Int get() = (((raw ushr SHIFT_X).toInt() and MASK_X) shl 10) shr 10
    val y: SectionHeight get() = (((raw ushr SHIFT_Y).toInt() and MASK_Y) shl 24) shr 24
    val z: Int get() = (((raw ushr SHIFT_Z).toInt() and MASK_Z) shl 10) shr 10
    val chunkPosition: ChunkPosition get() = ChunkPosition(x, z)
    fun sectionIndex(minSection: SectionHeight): SectionIndex = y - minSection
    fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = SectionPosition(x, y, z)
    operator fun plus(other: SectionPosition) = SectionPosition(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: SectionPosition) = SectionPosition(x - other.x, y - other.y, z - other.z)
    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z
    override fun toString() = "c($x $y $z)"
    companion object {
        const val BITS_X = 22
        const val MASK_X = (1 shl BITS_X) - 1
        const val SHIFT_X = 0
        const val BITS_Z = 22
        const val MASK_Z = (1 shl BITS_Z) - 1
        const val SHIFT_Z = 22
        const val BITS_Y = 8
        const val MASK_Y = 255
        const val SHIFT_Y = 44
        const val MAX_X = BlockPosition.MAX_X shr 4
        const val MAX_Z = BlockPosition.MAX_Z shr 4
        const val MIN_Y = BlockPosition.MIN_Y shr 4
        const val MAX_Y = BlockPosition.MAX_Y shr 4
        val EMPTY = SectionPosition(0, 0, 0)
        fun of(chunk: ChunkPosition, height: SectionHeight) = SectionPosition(chunk.x, height, chunk.z)
    }
}
