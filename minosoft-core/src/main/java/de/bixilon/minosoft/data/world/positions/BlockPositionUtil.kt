/*
 * Minosoft Copyright (C) 2020-2025 Moritz Zwerger.
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Adapted from Bixilon/Minosoft BlockPositionUtil.kt.
 * KMath vectors and DebugOptions omitted in this Android foundation port.
 */
package de.bixilon.minosoft.data.world.positions

object BlockPositionUtil {
    /** Upstream's position hash calculation; intentionally uses overflowing Long arithmetic. */
    fun generatePositionHash(x: Int, y: Int, z: Int): Long {
        var hash = (x * 3129871L) xor (z * 116129781L) xor y.toLong()
        hash = hash * hash * 42317861L + hash * 11L
        return hash shr 16
    }

    /** Android port enforces bounds instead of upstream's optional debug-only assertions. */
    fun assertPosition(value: Int, min: Int, max: Int) {
        require(value in min..max) { "coordinate out of range: $value not in [$min, $max]" }
    }

    fun assertPosition(condition: Boolean) {
        require(condition) { "Position assert failed!" }
    }

    fun assertPosition(condition: Boolean, message: String) {
        require(condition) { "Position assert failed: $message" }
    }
}
