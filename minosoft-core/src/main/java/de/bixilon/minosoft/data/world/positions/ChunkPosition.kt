/* Minosoft Copyright (C) 2020-2026 Moritz Zwerger
 * GPL-3.0-or-later. Android adaptation of upstream Minosoft world positions.
 * KMath/KUtil and rendering-specific dependencies intentionally omitted.
 */
package de.bixilon.minosoft.data.world.positions

import de.bixilon.minosoft.data.world.chunk.ChunkSize

/** Upstream Minosoft packed 32+32-bit chunk coordinate representation. */
@JvmInline
value class ChunkPosition(val raw: Long) {
    constructor(x: Int, z: Int) : this((z.toLong() shl SHIFT_Z) or (x.toLong() and 0xFFFFFFFFL))
    val x: Int get() = raw.toInt()
    val z: Int get() = (raw ushr SHIFT_Z).toInt()
    fun with(x: Int = this.x, z: Int = this.z) = ChunkPosition(x, z)
    fun plusX() = with(x = x + 1)
    fun minusX() = with(x = x - 1)
    fun plusZ() = with(z = z + 1)
    fun minusZ() = with(z = z - 1)
    operator fun plus(other: ChunkPosition) = ChunkPosition(x + other.x, z + other.z)
    operator fun minus(other: ChunkPosition) = ChunkPosition(x - other.x, z - other.z)
    fun blockPosition(x: Int, y: Int, z: Int) =
        BlockPosition(this.x * ChunkSize.SECTION_WIDTH_X + x, y, this.z * ChunkSize.SECTION_WIDTH_Z + z)
    operator fun component1() = x
    operator fun component2() = z
    override fun toString() = "c($x $z)"
    companion object {
        const val SHIFT_X = 0
        const val SHIFT_Z = 32
        const val BITS_X = 32
        const val BITS_Z = 32
        const val X = 1L
        const val Z = 1L shl 32
        val EMPTY = ChunkPosition(0, 0)
    }
}
