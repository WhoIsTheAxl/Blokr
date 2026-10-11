/*
 * Blokr Minosoft-core integration contracts.
 * Copyright (C) 2026 Blokr contributors.
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * NOT upstream Minosoft implementations. These compile-time contracts only
 * describe future integration boundaries. No runtime implementations provided.
 */
package de.bixilon.minosoft.portplan

import de.bixilon.minosoft.data.world.positions.*

/** Requires upstream dimension metadata and chunk manager. */
interface WorldPort {
    val chunks: ChunkManagerPort
    fun tick()
}

/** Requires upstream world update events and registry-backed block states. */
interface WorldUpdatePort {
    fun onChunkUnloaded(position: ChunkPosition)
}

/** Requires upstream light propagation and dimension skylight rules. */
interface LightingPort {
    fun onSectionChanged(position: SectionPosition)
}

/** Requires upstream entity registries and entity lifecycle. */
interface EntityManagerPort {
    fun tick()
}

/** Requires upstream collision shapes and block state collision rules. */
interface CollisionPort {
    fun isColliding(position: BlockPosition): Boolean
}
