package de.bixilon.minosoft.data.world.portable

import java.util.ArrayDeque

/**
 * Bounded staging queue between a packet reader and the portable world.
 * Commands are immutable snapshots at submission time. No protocol version assumptions.
 * Drain applies each command in FIFO order; a failed command stays at the head.
 */
class WorldUpdateQueue(private val maxPending: Int = 1024) {
    sealed class Command {
        data class BlockBatch(val updates: List<PortableWorld.BlockUpdate>) : Command()
        class ReplaceChunk(val x: Int, val z: Int, sections: Map<Int, IntArray>) : Command() {
            val sections: Map<Int, IntArray> = sections.mapValues { (_, value) -> value.clone() }
        }
        data class UnloadChunk(val x: Int, val z: Int) : Command()
        object Tick : Command()
    }

    private val pending = ArrayDeque<Command>()

    init { require(maxPending > 0) { "Queue capacity must be positive" } }

    @Synchronized
    fun submit(command: Command) {
        require(pending.size < maxPending) { "World update queue is full" }
        val copy = when (command) {
            is Command.BlockBatch -> Command.BlockBatch(command.updates.toList())
            is Command.ReplaceChunk -> Command.ReplaceChunk(command.x, command.z, command.sections)
            is Command.UnloadChunk -> command
            Command.Tick -> command
        }
        pending.addLast(copy)
    }

    @Synchronized fun size(): Int = pending.size

    /** Capacity remaining without dropping or overwriting pending world updates. */
    @Synchronized fun remainingCapacity(): Int = maxPending - pending.size

    /** Non-throwing backpressure API for network consumers. */
    @Synchronized
    fun trySubmit(command: Command): Boolean {
        if (pending.size >= maxPending) return false
        submit(command)
        return true
    }

    /** Applies up to limit commands. Invalid commands are not removed. */
    @Synchronized
    fun drainInto(world: PortableWorld, limit: Int = Int.MAX_VALUE): Int {
        require(limit >= 0) { "Drain limit must be nonnegative" }
        var applied = 0
        while (applied < limit && pending.isNotEmpty()) {
            when (val command = pending.peekFirst()) {
                is Command.BlockBatch -> world.applyBlockUpdates(command.updates)
                is Command.ReplaceChunk -> world.replaceChunk(command.x, command.z, command.sections)
                is Command.UnloadChunk -> world.unloadChunk(command.x, command.z)
                Command.Tick -> world.tick()
            }
            pending.removeFirst()
            applied++
        }
        return applied
    }

    @Synchronized fun clear() { pending.clear() }
}
