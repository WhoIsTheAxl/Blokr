package de.bixilon.minosoft.data.world.portable

import org.junit.Assert.*
import org.junit.Test

class CoreFoundationTest {
    @Test fun coordinateRoundTripsAndNegativeBoundaries() {
        for (value in listOf(Int.MIN_VALUE, -33, -17, -16, -1, 0, 15, 16, 17, Int.MAX_VALUE)) {
            val section = WorldCoordinates.section(value)
            val local = WorldCoordinates.local(value)
            assertTrue(local in 0..15)
            assertEquals(value.toLong(), section.toLong() * 16L + local)
        }
        for (i in 0 until 4096) {
            assertEquals(i, WorldCoordinates.index(
                WorldCoordinates.localX(i),
                WorldCoordinates.localY(i),
                WorldCoordinates.localZ(i)
            ))
        }
    }

    @Test fun catalogRejectsConflictsAndPreservesAir() {
        val catalog = BlockStateCatalog()
        assertEquals("minecraft:air", catalog.findById(0)?.name)
        catalog.register(1, "minecraft:stone")
        catalog.register(1, "minecraft:stone")
        assertEquals(2, catalog.size())
        assertEquals(1, catalog.findByName("minecraft:stone")?.id)
        try {
            catalog.register(2, "minecraft:stone")
            fail("Duplicate name accepted")
        } catch (_: IllegalArgumentException) {}
        try {
            catalog.register(0, "minecraft:dirt")
            fail("Air overwritten")
        } catch (_: IllegalArgumentException) {}
        assertNull(catalog.findById(999))
    }

    @Test fun blockBatchValidationAndLastWriteWins() {
        val world = PortableWorld()
        world.setBlock(0, 0, 0, 4)
        try {
            world.applyBlockUpdates(listOf(
                PortableWorld.BlockUpdate(0, 0, 0, 9),
                PortableWorld.BlockUpdate(1, 0, 0, -1)
            ))
            fail("Invalid batch accepted")
        } catch (_: IllegalArgumentException) {}
        assertEquals(4, world.getBlock(0, 0, 0))
        world.applyBlockUpdates(listOf(
            PortableWorld.BlockUpdate(-1, 0, -1, 2),
            PortableWorld.BlockUpdate(-1, 0, -1, 3),
            PortableWorld.BlockUpdate(0, 0, 0, 0)
        ))
        assertEquals(3, world.getBlock(-1, 0, -1))
        assertEquals(0, world.getBlock(0, 0, 0))
        assertEquals(1, world.loadedChunks())
    }

    @Test fun chunkSnapshotIsDefensiveAndSorted() {
        val world = PortableWorld()
        world.setBlock(-16, 32, -16, 3)
        world.setBlock(-16, 0, -16, 1)
        world.setBlock(0, 0, 0, 5)
        assertEquals(2, world.loadedChunks())
        assertEquals(setOf(
            PortableWorld.ChunkPosition(-1, -1),
            PortableWorld.ChunkPosition(0, 0)
        ), world.chunkPositions())
        val snapshot = world.chunkSnapshot(-1, -1)
        assertEquals(listOf(0, 2), snapshot.keys.toList())
        snapshot.getValue(0)[0] = 99
        assertEquals(1, world.getBlock(-16, 0, -16))
        world.replaceChunk(-1, -1, emptyMap())
        assertEquals(1, world.loadedChunks())
        assertEquals(5, world.getBlock(0, 0, 0))
    }
}
