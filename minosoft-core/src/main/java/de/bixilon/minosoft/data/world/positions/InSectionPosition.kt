/* Minosoft Copyright (C) 2020-2026 Moritz Zwerger
 * GPL-3.0-or-later. Android adaptation of upstream Minosoft world positions.
 * KMath/KUtil and rendering-specific dependencies intentionally omitted.
 */
package de.bixilon.minosoft.data.world.positions

/** Upstream Minosoft 12-bit YZX section position; index layout is unchanged. */
@JvmInline
value class InSectionPosition(val index: Int) {
    constructor() : this(0, 0, 0)
    constructor(x: Int, y: Int, z: Int) : this((y shl SHIFT_Y) or (z shl SHIFT_Z) or x) {
        require(x in 0..15 && y in 0..15 && z in 0..15)
    }
    init { require(index in 0..4095) { "section index out of range: $index" } }
    fun plusX(delta: Int) = with(x = x + delta)
    fun plusY(delta: Int) = with(y = y + delta)
    fun plusZ(delta: Int) = with(z = z + delta)
    operator fun plus(other: InSectionPosition) =
        InSectionPosition(x + other.x, y + other.y, z + other.z)
    val x: Int get() = (index ushr SHIFT_X) and MASK_X
    val y: Int get() = (index ushr SHIFT_Y) and MASK_Y
    val z: Int get() = (index ushr SHIFT_Z) and MASK_Z
    val xz: Int get() = index and 0xFF
    fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = InSectionPosition(x, y, z)
    fun plusX() = with(x = x + 1)
    fun minusX() = with(x = x - 1)
    fun plusY() = with(y = y + 1)
    fun minusY() = with(y = y - 1)
    fun plusZ() = with(z = z + 1)
    fun minusZ() = with(z = z - 1)
    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z
    override fun toString() = "s($x $y $z)"
    companion object {
        const val BITS_X = 4
        const val MASK_X = 15
        const val SHIFT_X = 0
        const val BITS_Z = 4
        const val MASK_Z = 15
        const val SHIFT_Z = 4
        const val BITS_Y = 4
        const val MASK_Y = 15
        const val SHIFT_Y = 8
        const val X = 1
        const val Z = 16
        const val Y = 256
        val EMPTY = InSectionPosition(0, 0, 0)
    }
}
