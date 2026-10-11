package de.bixilon.minosoft.data.world.portable

import org.junit.Assert.*
import org.junit.Test

class WorldPipelineTest {
    @Test fun singleValueSection() {
        val states = PalettedSectionDecoder.decode(0, intArrayOf(42), longArrayOf())
        assertEquals(4096, states.size)
        assertTrue(states.all { it == 42 })
    }

    @Test fun paddedPaletteRoundTripsAcrossWordBoundaries() {
        for (bits in listOf(1, 4, 5, 6, 8, 13, 32)) {
            val palette = if (bits == 1) intArrayOf(0, 7) else intArrayOf(0, 7, 9, 13)
            val indices = IntArray(4096) { (it * 17 + it / 13) % palette.size }
            val packed = PalettedSectionDecoder.encode(bits, indices)
            val decoded = PalettedSectionDecoder.decode(bits, palette, packed)
            assertArrayEquals(IntArray(4096) { palette[indices[it]] }, decoded)
        }
    }

    @Test fun rejectsInvalidPaletteIndex() {
        val packed = PalettedSectionDecoder.encode(4, IntArray(4096) { 3 })
        try {
            PalettedSectionDecoder.decode(4, intArrayOf(0, 1), packed)
            fail("Invalid palette index accepted")
        } catch (_: IllegalArgumentException) {}
    }

    @Test fun queuePreservesOrderAndCopiesSections() {
        val world = PortableWorld()
        val queue = WorldUpdateQueue(4)
        val section = IntArray(4096)
        section[0] = 11
        queue.submit(WorldUpdateQueue.Command.ReplaceChunk(0, 0, mapOf(0 to section)))
        section[0] = 99
        queue.submit(WorldUpdateQueue.Command.BlockBatch(listOf(
            PortableWorld.BlockUpdate(1, 0, 0, 12)
        )))
        queue.submit(WorldUpdateQueue.Command.Tick)
        assertEquals(1, queue.drainInto(world, 1))
        assertEquals(11, world.getBlock(0, 0, 0))
        assertEquals(0, world.getBlock(1, 0, 0))
        assertEquals(2, queue.drainInto(world))
        assertEquals(12, world.getBlock(1, 0, 0))
        assertEquals(1L, world.tickCount)
    }

    @Test fun failedCommandRemainsQueued() {
        val world = PortableWorld()
        val queue = WorldUpdateQueue()
        queue.submit(WorldUpdateQueue.Command.BlockBatch(listOf(
            PortableWorld.BlockUpdate(0, 0, 0, -1)
        )))
        try {
            queue.drainInto(world)
            fail("Invalid command accepted")
        } catch (_: IllegalArgumentException) {}
        assertEquals(1, queue.size())
        assertEquals(0, world.loadedSections())
        queue.clear()
        assertEquals(0, queue.size())
    }

    @Test fun capacityAndDrainLimits() {
        val world = PortableWorld()
        val queue = WorldUpdateQueue(1)
        queue.submit(WorldUpdateQueue.Command.Tick)
        try {
            queue.submit(WorldUpdateQueue.Command.Tick)
            fail("Queue overflow accepted")
        } catch (_: IllegalArgumentException) {}
        assertEquals(0, queue.drainInto(world, 0))
        assertEquals(1, queue.drainInto(world))
        assertEquals(1L, world.tickCount)
    }
}
