package de.bixilon.minosoft.data.world.portable

import de.bixilon.minosoft.data.world.chunk.ChunkSize
import org.junit.Assert.*
import org.junit.Test

class PortableWorldTest {
    @Test fun negativeCoordinatesAndSectionBoundaries() {
        val world = PortableWorld()
        world.setBlock(-1, -1, -17, 42)
        world.setBlock(16, 64, 16, 7)
        assertEquals(42, world.getBlock(-1, -1, -17))
        assertEquals(7, world.getBlock(16, 64, 16))
        assertEquals(0, world.getBlock(-1, -1, -16))
        assertEquals(2, world.loadedSections())
        world.setBlock(-1, -1, -17, 0)
        assertEquals(1, world.loadedSections())
    }

    @Test fun snapshotsAreIndependentAndReplacementCopiesInput() {
        val world = PortableWorld()
        val states = IntArray(ChunkSize.BLOCKS_PER_SECTION)
        states[0] = 11
        states[4095] = 12
        world.replaceSection(0, 4, 0, states)
        states[0] = 99
        assertEquals(11, world.getBlock(0, 64, 0))
        assertEquals(12, world.getBlock(15, 79, 15))
        val snapshot = world.sectionSnapshot(0, 4, 0)
        snapshot[0] = 88
        assertEquals(11, world.getBlock(0, 64, 0))
        world.replaceSection(0, 4, 0, IntArray(4096))
        assertEquals(0, world.loadedSections())
    }

    @Test fun invalidReplacementDoesNotMutateWorld() {
        val world = PortableWorld()
        world.setBlock(0, 0, 0, 2)
        try {
            world.replaceSection(0, 0, 0, IntArray(4096) { if (it == 3) -1 else 0 })
            fail("Expected invalid state rejection")
        } catch (_: IllegalArgumentException) {}
        assertEquals(2, world.getBlock(0, 0, 0))
    }

    @Test fun worldHeightAndTick() {
        val world = PortableWorld()
        world.setBlock(0, ChunkSize.CHUNK_MIN_Y, 0, 1)
        world.setBlock(0, ChunkSize.CHUNK_MAX_Y - 1, 0, 2)
        assertEquals(0, world.getBlock(0, ChunkSize.CHUNK_MAX_Y, 0))
        world.tick()
        assertEquals(1L, world.tickCount)
        world.clear()
        assertEquals(0L, world.tickCount)
        assertEquals(0, world.loadedSections())
    }
}
