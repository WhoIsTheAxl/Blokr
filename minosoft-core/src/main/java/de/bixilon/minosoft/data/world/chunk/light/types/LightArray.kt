/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Adapted for Android: removes desktop DebugOptions and KUtil hex dependencies.
 */
package de.bixilon.minosoft.data.world.chunk.light.types

import de.bixilon.minosoft.data.world.chunk.ChunkSize
import java.util.Arrays

/**
 * Minosoft section light array, adapted to use an integer section index while
 * upstream InSectionPosition (and its KMath dependencies) are not yet ported.
 * One byte per block; each byte stores block and sky light nibbles.
 */
@JvmInline
value class LightArray(val array: ByteArray = ByteArray(ChunkSize.BLOCKS_PER_SECTION)) {
    operator fun get(index: Int): LightLevel {
        require(index in 0 until ChunkSize.BLOCKS_PER_SECTION)
        return LightLevel(array[index])
    }

    operator fun set(index: Int, value: LightLevel) {
        require(index in 0 until ChunkSize.BLOCKS_PER_SECTION)
        array[index] = value.raw
    }

    fun clear() = Arrays.fill(array, 0.toByte())
}
