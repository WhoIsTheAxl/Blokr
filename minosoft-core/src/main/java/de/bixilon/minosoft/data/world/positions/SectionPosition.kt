/* Minosoft Copyright (C) 2020-2026 Moritz Zwerger
 * GPL-3.0-or-later. Adapted from upstream for Android without KMath/KUtil.
 */
package de.bixilon.minosoft.data.world.positions

/** Upstream Minosoft packed section coordinates: 22-bit X/Z, 8-bit Y. */
@JvmInline
value class SectionPosition(val raw: Long) {
    constructor() : this(0, 0, 0)
    constructor(x: Int, y: SectionHeight, z: Int) : this(
        ((y.toLong() and MASK_Y.toLong()) shl SHIFT_Y) or
        ((z.toLong() and MASK_Z.toLong()) shl SHIFT_Z) or
        (x.toLong() and MASK_X.toLong())
    ) {
        require(x in -MAX_X..MAX_X && z in -MAX_Z..MAX_Z)
        require(y in MIN_Y..MAX_Y)
    }
    fun modify(other: Long, component: Long, add: Long): SectionPosition =
        SectionPosition((raw and other) or (((raw and component) + add) and component))
    fun modifyX(add: Long) = modify((MASK_Z.toLong() shl SHIFT_Z) or (MASK_Y.toLong() shl SHIFT_Y), MASK_X.toLong(), add)
    fun modifyY(add: Long) = modify(MASK_X.toLong() or (MASK_Z.toLong() shl SHIFT_Z), MASK_Y.toLong() shl SHIFT_Y, add)
    fun modifyZ(add: Long) = modify(MASK_X.toLong() or (MASK_Y.toLong() shl SHIFT_Y), MASK_Z.toLong() shl SHIFT_Z, add)
    val x: Int get() = (((raw ushr SHIFT_X).toInt() and MASK_X) shl 10) shr 10
    val y: SectionHeight get() = (((raw ushr SHIFT_Y).toInt() and MASK_Y) shl 24) shr 24
    val z: Int get() = (((raw ushr SHIFT_Z).toInt() and MASK_Z) shl 10) shr 10
    val chunkPosition: ChunkPosition get() = ChunkPosition(x, z)
    fun sectionIndex(minSection: SectionHeight): SectionIndex = y - minSection
    fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = SectionPosition(x, y, z)
    fun plusX(delta: Int = 1) = with(x = x + delta)
    fun plusY(delta: Int = 1) = with(y = y + delta)
    fun plusZ(delta: Int = 1) = with(z = z + delta)
    fun minusX() = plusX(-1)
    fun minusY() = plusY(-1)
    fun minusZ() = plusZ(-1)
    operator fun plus(value: Int) = SectionPosition(x + value, y + value, z + value)
    operator fun minus(value: Int) = SectionPosition(x - value, y - value, z - value)
    operator fun times(value: Int) = SectionPosition(x * value, y * value, z * value)
    operator fun div(value: Int) = SectionPosition(x / value, y / value, z / value)
    operator fun plus(other: ChunkPosition) = SectionPosition(x + other.x, y, z + other.z)
    operator fun minus(other: ChunkPosition) = SectionPosition(x - other.x, y, z - other.z)
    operator fun unaryMinus() = SectionPosition(-x, -y, -z)
    operator fun unaryPlus() = this
    fun length2() = x.toLong() * x + y.toLong() * y + z.toLong() * z
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
        const val X = 1L shl SHIFT_X
        const val Z = 1L shl SHIFT_Z
        const val Y = 1L shl SHIFT_Y
        const val MAX_X = BlockPosition.MAX_X shr 4
        const val MAX_Z = BlockPosition.MAX_Z shr 4
        const val MIN_Y = BlockPosition.MIN_Y shr 4
        const val MAX_Y = BlockPosition.MAX_Y shr 4
        val EMPTY = SectionPosition(0, 0, 0)
        fun of(chunk: ChunkPosition, index: SectionIndex, minSection: SectionHeight) =
            SectionPosition(chunk.x, index + minSection, chunk.z)
        fun of(chunk: ChunkPosition, height: SectionHeight) = SectionPosition(chunk.x, height, chunk.z)
    }
}
