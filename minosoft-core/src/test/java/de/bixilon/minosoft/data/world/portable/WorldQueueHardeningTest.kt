package de.bixilon.minosoft.data.world.portable

import org.junit.Assert.*
import org.junit.Test

class WorldQueueHardeningTest {
    @Test fun queueBackpressureNeverDiscardsOldestCommand() {
        val queue = WorldUpdateQueue(1)
        val world = PortableWorld()
        assertEquals(1, queue.remainingCapacity())
        assertTrue(queue.trySubmit(WorldUpdateQueue.Command.BlockBatch(
            listOf(PortableWorld.BlockUpdate(0, 0, 0, 42)))))
        assertFalse(queue.trySubmit(WorldUpdateQueue.Command.BlockBatch(
            listOf(PortableWorld.BlockUpdate(0, 0, 0, 99)))))
        assertEquals(0, queue.remainingCapacity())
        assertEquals(1, queue.drainInto(world))
        assertEquals(42, world.getBlock(0, 0, 0))
        assertEquals(1, queue.remainingCapacity())
    }

    @Test fun rejectedCommandKeepsFailedCommandAtHead() {
        val queue = WorldUpdateQueue(1)
        val world = PortableWorld()
        assertTrue(queue.trySubmit(WorldUpdateQueue.Command.BlockBatch(
            listOf(PortableWorld.BlockUpdate(0, 2048, 0, 1)))))
        try {
            queue.drainInto(world)
            fail("Invalid world update should fail")
        } catch (_: IllegalArgumentException) {}
        assertEquals(1, queue.size())
        assertFalse(queue.trySubmit(WorldUpdateQueue.Command.Tick))
        assertEquals(0L, world.tickCount)
    }

    @Test fun replacementSectionsAreSnapshottedAtSubmission() {
        val queue = WorldUpdateQueue(1)
        val world = PortableWorld()
        val states = IntArray(4096)
        states[0] = 7
        assertTrue(queue.trySubmit(WorldUpdateQueue.Command.ReplaceChunk(0, 0, mapOf(0 to states))))
        states[0] = 99
        queue.drainInto(world)
        assertEquals(7, world.getBlock(0, 0, 0))
    }
}
