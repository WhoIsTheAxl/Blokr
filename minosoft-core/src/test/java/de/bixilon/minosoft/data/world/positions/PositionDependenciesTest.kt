package de.bixilon.minosoft.data.world.positions

import org.junit.Assert.*
import org.junit.Test

class PositionDependenciesTest {
    @Test fun inChunkPackedSignedHeightRoundTrip() {
        for (y in listOf(-2048, -17, -16, -1, 0, 15, 16, 2047)) {
            for (x in listOf(0, 7, 15)) for (z in listOf(0, 8, 15)) {
                val p = InChunkPosition(x, y, z)
                assertEquals(x, p.x)
                assertEquals(y, p.y)
                assertEquals(z, p.z)
                assertEquals(y shr 4, p.sectionHeight)
                assertEquals(y and 15, p.inSectionPosition.y)
                assertEquals((z shl 4) or x, p.xz)
            }
        }
    }

    @Test fun sectionPackedSignedCoordinatesRoundTrip() {
        for (x in listOf(-1_875_000, -17, -1, 0, 1, 1_875_000)) {
            for (y in listOf(-128, -1, 0, 1, 127)) {
                val p = SectionPosition(x, y, -x)
                assertEquals(x, p.x)
                assertEquals(y, p.y)
                assertEquals(-x, p.z)
                assertEquals(ChunkPosition(x, -x), p.chunkPosition)
                assertEquals(y + 128, p.sectionIndex(-128))
            }
        }
    }

    @Test fun rejectsInvalidPositions() {
        try {
            InChunkPosition(16, 0, 0)
            fail("Accepted invalid local X")
        } catch (_: IllegalArgumentException) {}
        try {
            InChunkPosition(0, 2048, 0)
            fail("Accepted invalid height")
        } catch (_: IllegalArgumentException) {}
        try {
            SectionPosition(0, 128, 0)
            fail("Accepted invalid section height")
        } catch (_: IllegalArgumentException) {}
    }
}
