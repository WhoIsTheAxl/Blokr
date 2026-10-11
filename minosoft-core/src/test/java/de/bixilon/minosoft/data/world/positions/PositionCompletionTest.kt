package de.bixilon.minosoft.data.world.positions

import org.junit.Assert.*
import org.junit.Test

class PositionCompletionTest {
    @Test fun packedMutationsPreserveUnchangedAxes() {
        val block = BlockPosition(-17, -4, 22)
        assertEquals(BlockPosition(-16, -4, 22), block.modifyX(BlockPosition.X))
        assertEquals(BlockPosition(-17, -3, 22), block.modifyY(BlockPosition.Y))
        assertEquals(BlockPosition(-17, -4, 23), block.modifyZ(BlockPosition.Z))

        val section = SectionPosition(-12, -3, 9)
        assertEquals(SectionPosition(-11, -3, 9), section.modifyX(SectionPosition.X))
        assertEquals(SectionPosition(-12, -2, 9), section.modifyY(SectionPosition.Y))
        assertEquals(SectionPosition(-12, -3, 10), section.modifyZ(SectionPosition.Z))

        val chunk = ChunkPosition(-4, 9)
        assertEquals(ChunkPosition(-3, 9), chunk.modifyX(ChunkPosition.X))
        assertEquals(ChunkPosition(-4, 10), chunk.modifyZ(ChunkPosition.Z))
    }

    @Test fun constructorsAndClampBehaveAtBounds() {
        assertEquals(BlockPosition.EMPTY, BlockPosition())
        assertEquals(ChunkPosition.EMPTY, ChunkPosition())
        assertEquals(SectionPosition.EMPTY, SectionPosition())
        assertEquals(InChunkPosition.EMPTY, InChunkPosition())
        assertEquals(InSectionPosition.EMPTY, InSectionPosition())
        assertEquals(BlockPosition(15, -5, 8), BlockPosition(InChunkPosition(15, -5, 8)))
        with(BlockPosition) {
            assertEquals(MAX_X - 1, Int.MAX_VALUE.clampX())
            assertEquals(MIN_Y + 1, Int.MIN_VALUE.clampY())
            assertEquals(-MAX_Z + 1, Int.MIN_VALUE.clampZ())
        }
    }

    @Test fun movementWithOffsetsChecksLocalBoundaries() {
        assertEquals(InSectionPosition(15, 14, 13), InSectionPosition(14, 13, 12).plusX(1).plusY(1).plusZ(1))
        assertEquals(InChunkPosition(15, -1, 3), InChunkPosition(14, -2, 2).plusX(1).plusY(1).plusZ(1))
        try {
            InSectionPosition(15, 0, 0).plusX(1)
            fail("Expected boundary validation")
        } catch (_: IllegalArgumentException) {}
    }
}
