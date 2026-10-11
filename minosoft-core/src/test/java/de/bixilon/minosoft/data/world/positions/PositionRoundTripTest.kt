package de.bixilon.minosoft.data.world.positions

import org.junit.Assert.*
import org.junit.Test

class PositionRoundTripTest {
    @Test fun allLocalSectionCoordinatesReconstructBlock() {
        val section = SectionPosition(-2, -4, 3)
        for (index in 0..4095) {
            val local = InSectionPosition(index)
            val block = BlockPosition.of(section, local)
            assertEquals(section, block.sectionPosition)
            assertEquals(local, block.inSectionPosition)
            assertEquals(block, BlockPosition.of(block.chunkPosition, block.sectionHeight, local))
        }
    }

    @Test fun negativeWorldBoundariesAndChunkReconstruction() {
        for (x in listOf(-30_000_000, -17, -16, -1, 0, 15, 16, 30_000_000))
            for (z in listOf(-30_000_000, -1, 0, 30_000_000))
                for (y in listOf(-2048, -1, 0, 2047)) {
                    val block = BlockPosition(x, y, z)
                    assertEquals(block, BlockPosition.of(block.chunkPosition, block.inChunkPosition))
                    assertEquals(block, BlockPosition.of(block.sectionPosition, block.inSectionPosition))
                }
    }

    @Test fun positionArithmeticAndBounds() {
        assertEquals(SectionPosition(1, -2, 3), SectionPosition(0, -2, 3).plusX())
        assertEquals(ChunkPosition(-2, 3), ChunkPosition(-1, 3).plusX(-1))
        assertEquals(BlockPosition(-1, 2, 3), BlockPosition(0, 2, 3).minusX())
        try {
            InSectionPosition(4096)
            fail("Out-of-range packed section index was accepted")
        } catch (_: IllegalArgumentException) {}
    }
}
