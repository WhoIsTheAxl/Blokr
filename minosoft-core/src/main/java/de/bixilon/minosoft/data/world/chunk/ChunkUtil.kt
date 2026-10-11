/* Minosoft Copyright (C) 2020-2026 Moritz Zwerger
 * GPL-3.0-or-later. Android adaptation of upstream Minosoft world positions.
 * KMath/KUtil and rendering-specific dependencies intentionally omitted.
 */
package de.bixilon.minosoft.data.world.chunk

import de.bixilon.minosoft.data.world.positions.ChunkPosition
import java.lang.StrictMath.abs

/** Upstream square view-distance check, without KMath's vector dependency. */
object ChunkUtil {
    fun ChunkPosition.isInViewDistance(viewDistance: Int, cameraPosition: ChunkPosition): Boolean {
        require(viewDistance >= 0)
        return abs(cameraPosition.x.toLong() - x.toLong()) <= viewDistance.toLong() &&
            abs(cameraPosition.z.toLong() - z.toLong()) <= viewDistance.toLong()
    }
}
