/* Minosoft Copyright (C) 2020-2026 Moritz Zwerger
 * GPL-3.0-or-later. Android adaptation of upstream Minosoft world positions.
 * KMath/KUtil and rendering-specific dependencies intentionally omitted.
 */
package de.bixilon.minosoft.data.world.positions

/** Minosoft packed 26-bit X/Z and 12-bit Y block coordinates. */
@JvmInline
value class BlockPosition(val raw: Long) {
    constructor(x: Int, y: Int, z: Int) : this(
        ((y.toLong() and MASK_Y.toLong()) shl SHIFT_Y) or
        ((z.toLong() and MASK_Z.toLong()) shl SHIFT_Z) or
        (x.toLong() and MASK_X.toLong())
    ) {
        require(x in -MAX_X..MAX_X && z in -MAX_Z..MAX_Z)
        require(y in MIN_Y..MAX_Y)
    }
    val x: Int get() = (((raw ushr SHIFT_X).toInt() and MASK_X) shl 6) shr 6
    val y: Int get() = (((raw ushr SHIFT_Y).toInt() and MASK_Y) shl 20) shr 20
    val z: Int get() = (((raw ushr SHIFT_Z).toInt() and MASK_Z) shl 6) shr 6
    val hash: Long get() = BlockPositionUtil.generatePositionHash(x, y, z)
    val sectionHeight: SectionHeight get() = y shr 4
    val sectionPosition: SectionPosition get() = SectionPosition(x shr 4, y shr 4, z shr 4)
    val inChunkPosition: InChunkPosition get() = InChunkPosition(x and 15, y, z and 15)
    val chunkPosition: ChunkPosition get() = ChunkPosition(x shr 4, z shr 4)
    val inSectionPosition: InSectionPosition get() = InSectionPosition(x and 15, y and 15, z and 15)
    fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = BlockPosition(x, y, z)
    fun plusX(delta: Int = 1) = with(x = x + delta)
    fun minusX() = plusX(-1)
    fun plusY(delta: Int = 1) = with(y = y + delta)
    fun minusY() = plusY(-1)
    fun plusZ(delta: Int = 1) = with(z = z + delta)
    fun minusZ() = plusZ(-1)
    operator fun plus(value: Int) = BlockPosition(x + value, y + value, z + value)
    operator fun minus(value: Int) = BlockPosition(x - value, y - value, z - value)
    operator fun times(value: Int) = BlockPosition(x * value, y * value, z * value)
    operator fun div(value: Int) = BlockPosition(x / value, y / value, z / value)
    operator fun unaryMinus() = BlockPosition(-x, -y, -z)
    operator fun unaryPlus() = this
    operator fun minus(other: InChunkPosition) = BlockPosition(x - other.x, y - other.y, z - other.z)
    operator fun minus(other: InSectionPosition) = BlockPosition(x - other.x, y - other.y, z - other.z)
    fun length2() = x.toLong() * x + y.toLong() * y + z.toLong() * z
    infix fun and(mask: Int) = BlockPosition(x and mask, y and mask, z and mask)
    operator fun plus(other: InChunkPosition) = BlockPosition(x + other.x, y + other.y, z + other.z)
    operator fun plus(other: InSectionPosition) = BlockPosition(x + other.x, y + other.y, z + other.z)
    operator fun plus(other: BlockPosition) = BlockPosition(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: BlockPosition) = BlockPosition(x - other.x, y - other.y, z - other.z)
    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z
    override fun toString() = "b($x $y $z)"
    companion object {
        const val BITS_X = 26
        const val MASK_X = (1 shl BITS_X) - 1
        const val SHIFT_X = 0
        const val BITS_Z = 26
        const val MASK_Z = (1 shl BITS_Z) - 1
        const val SHIFT_Z = 26
        const val BITS_Y = 12
        const val MASK_Y = (1 shl BITS_Y) - 1
        const val SHIFT_Y = 52
        const val MAX_X = 30_000_000
        const val MAX_Z = 30_000_000
        const val MIN_Y = -2048
        const val MAX_Y = 2047
        val EMPTY = BlockPosition(0, 0, 0)
        fun of(chunk: ChunkPosition, inChunk: InChunkPosition) =
            BlockPosition((chunk.x shl 4) or inChunk.x, inChunk.y, (chunk.z shl 4) or inChunk.z)
        fun of(section: SectionPosition, inSection: InSectionPosition) =
            BlockPosition((section.x shl 4) or inSection.x, (section.y shl 4) or inSection.y, (section.z shl 4) or inSection.z)
        fun of(section: SectionPosition) =
            BlockPosition(section.x shl 4, section.y shl 4, section.z shl 4)
        fun of(chunk: ChunkPosition, sectionHeight: Int) =
            BlockPosition(chunk.x shl 4, sectionHeight shl 4, chunk.z shl 4)
        fun of(chunk: ChunkPosition, sectionHeight: Int, inSection: InSectionPosition) =
            BlockPosition((chunk.x shl 4) or inSection.x, (sectionHeight shl 4) or inSection.y,
                (chunk.z shl 4) or inSection.z)
    }
}
