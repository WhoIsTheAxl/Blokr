/* Minosoft Copyright (C) 2020-2026 Moritz Zwerger
 * GPL-3.0-or-later. Adapted from upstream for Android without KMath/KUtil.
 */
package de.bixilon.minosoft.data.world.positions

import de.bixilon.minosoft.data.world.chunk.ChunkSize

/** Upstream Minosoft packed 4/4/12-bit X/Z/Y in-chunk coordinates. */
@JvmInline
value class InChunkPosition(val raw: Int) {
    constructor(x: Int, y: Int, z: Int) : this(
        ((y and MASK_Y) shl SHIFT_Y) or (z shl SHIFT_Z) or x
    ) {
        require(x in 0..15 && z in 0..15)
        require(y >= ChunkSize.CHUNK_MIN_Y && y < ChunkSize.CHUNK_MAX_Y)
    }

    val x: Int get() = (raw ushr SHIFT_X) and MASK_X
    val y: Int get() = (((raw ushr SHIFT_Y) and MASK_Y) shl 20) shr 20
    val z: Int get() = (raw ushr SHIFT_Z) and MASK_Z
    val xz: Int get() = raw and 0xFF
    val inSectionPosition: InSectionPosition get() = InSectionPosition(x, y and 15, z)
    val sectionHeight: SectionHeight get() = y shr 4

    fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = InChunkPosition(x, y, z)
    fun plusX() = with(x = x + 1)
    fun minusX() = with(x = x - 1)
    fun plusY() = with(y = y + 1)
    fun minusY() = with(y = y - 1)
    fun plusZ() = with(z = z + 1)
    fun minusZ() = with(z = z - 1)
    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z
    override fun toString() = "c($x $y $z)"

    companion object {
        const val BITS_X = 4
        const val MASK_X = 15
        const val SHIFT_X = 0
        const val BITS_Z = 4
        const val MASK_Z = 15
        const val SHIFT_Z = 4
        const val BITS_Y = 12
        const val MASK_Y = 4095
        const val SHIFT_Y = 8
        const val X = 1
        const val Z = 16
        const val Y = 256
        val EMPTY = InChunkPosition(0, 0, 0)
    }
}
