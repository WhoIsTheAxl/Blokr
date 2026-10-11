package de.bixilon.minosoft.data.world.positions

import org.junit.Assert.*
import org.junit.Test

class PositionExpansionTest {
    @Test fun negativeCoordinateSectionConversions() {
        val block = BlockPosition(-17, -17, 31)
        assertEquals(SectionPosition(-2, -2, 1), block.sectionPosition)
        assertEquals(-2, block.sectionHeight)
        assertEquals(InChunkPosition(15, -17, 15), block.inChunkPosition)
        assertEquals(ChunkPosition(-2, 1), block.chunkPosition)
        assertEquals(InSectionPosition(15, 15, 15), block.inSectionPosition)
    }

    @Test fun reconstructBlockFromChunkSectionAndLocalPosition() {
        val original = BlockPosition(-17, -17, 31)
        val reconstructed = BlockPosition.of(
            original.chunkPosition, original.sectionHeight, original.inSectionPosition
        )
        assertEquals(original, reconstructed)
    }

    @Test fun hashUsesUpstreamAlgorithm() {
        val block = BlockPosition(13, -5, 22)
        assertEquals(BlockPositionUtil.generatePositionHash(13, -5, 22), block.hash)
        assertEquals(BlockPosition(14, -5, 22), block.plusX())
        assertEquals(BlockPosition(13, -4, 22), block.plusY())
        assertEquals(BlockPosition(13, -5, 21), block.minusZ())
    }
}
