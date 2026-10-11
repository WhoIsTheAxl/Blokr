/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Adapted for Android: removes desktop DebugOptions and KUtil hex dependencies.
 */
package de.bixilon.minosoft.data.world.chunk.light.types

/**
 * Minosoft's packed light representation: lower nibble block light, upper nibble sky light.
 * Adaptation: strict constructor validation replaces optional desktop debug assertions.
 */
@JvmInline
value class LightLevel(val raw: Byte) {
    constructor(block: Int, sky: Int) : this(((block shl BLOCK_SHIFT) or (sky shl SKY_SHIFT)).toByte()) {
        require(block in MIN_LEVEL..MAX_LEVEL) { "Block light must be in 0..15" }
        require(sky in MIN_LEVEL..MAX_LEVEL) { "Sky light must be in 0..15" }
    }

    val block: Int get() = (raw.toInt() ushr BLOCK_SHIFT) and BLOCK_MASK
    val sky: Int get() = (raw.toInt() ushr SKY_SHIFT) and SKY_MASK
    val index: Int get() = raw.toInt() and 0xFF

    fun with(block: Int = this.block, sky: Int = this.sky) = LightLevel(block, sky)
    fun max(other: LightLevel) = LightLevel(maxOf(block, other.block), maxOf(sky, other.sky))
    override fun toString(): String = index.toString(16).padStart(2, '0')

    companion object {
        const val BITS = 4
        const val BLOCK_SHIFT = 0
        const val BLOCK_MASK = 0x0F
        const val SKY_SHIFT = 4
        const val SKY_MASK = 0x0F
        const val MIN_LEVEL = 0
        const val MAX_LEVEL = (1 shl BITS) - 1
        const val LEVELS = 1 shl BITS
        val EMPTY = LightLevel(MIN_LEVEL, MIN_LEVEL)
        val MAX = LightLevel(MAX_LEVEL, MAX_LEVEL)
    }
}
