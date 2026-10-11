package de.bixilon.minosoft.data.world.positions

import de.bixilon.minosoft.data.world.chunk.ChunkUtil
import org.junit.Assert.*
import org.junit.Test

class PositionPortTest {
    @Test fun sectionIndexMatchesUpstreamYzx() {
        for (i in 0..4095) {
            val pos = InSectionPosition(i)
            assertEquals(i, InSectionPosition(pos.x, pos.y, pos.z).index)
        }
        assertEquals(0xABC, InSectionPosition(12, 10, 11).index)
        assertEquals(0, InSectionPosition.EMPTY.index)
    }

    @Test fun blockPackedCoordinatesRoundTrip() {
        val xs = listOf(-30_000_000, -17, -1, 0, 1, 15, 30_000_000)
        val ys = listOf(-2048, -1, 0, 1, 2047)
        for (x in xs) for (y in ys) {
            val pos = BlockPosition(x, y, -x)
            assertEquals(x, pos.x)
            assertEquals(y, pos.y)
            assertEquals(-x, pos.z)
            assertEquals(x shr 4, pos.chunkPosition.x)
            assertEquals((-x) shr 4, pos.chunkPosition.z)
            assertEquals(x and 15, pos.inSectionPosition.x)
            assertEquals(y and 15, pos.inSectionPosition.y)
        }
    }

    @Test fun chunkPackedCoordinatesRoundTrip() {
        for (x in listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE)) {
            val pos = ChunkPosition(x, -17)
            assertEquals(x, pos.x)
            assertEquals(-17, pos.z)
        }
        assertEquals(BlockPosition(-1, 64, -1), ChunkPosition(-1, -1).blockPosition(15, 64, 15))
    }

    @Test fun viewDistanceIsSquareAndOverflowSafe() {
        val center = ChunkPosition(0, 0)
        assertTrue(with(ChunkUtil) { ChunkPosition(3, -3).isInViewDistance(3, center) })
        assertFalse(with(ChunkUtil) { ChunkPosition(4, 0).isInViewDistance(3, center) })
        assertFalse(with(ChunkUtil) {
            ChunkPosition(Int.MIN_VALUE, 0).isInViewDistance(5, ChunkPosition(Int.MAX_VALUE, 0))
        })
    }
}
