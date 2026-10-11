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

/** Requires original BlockState and BlockRegistry ports before implementation. */
interface BlockRegistryPort {
    fun hasState(protocolStateId: Int): Boolean
}

/** Requires upstream block section provider and palette container. */
interface BlockSectionPort {
    fun isEmpty(): Boolean
}

/** Requires original biome registry and biome section provider. */
interface BiomeSectionPort {
    fun isEmpty(): Boolean
}

/** Requires upstream block entity data provider and tick lifecycle. */
interface BlockEntitySectionPort {
    fun tick()
}

/** Requires upstream ChunkSection, section light and providers. */
interface ChunkSectionPort {
    val height: SectionHeight
    val blocks: BlockSectionPort
    val biomes: BiomeSectionPort
    val blockEntities: BlockEntitySectionPort
}

/** Requires upstream Chunk and ChunkSectionManagement. */
interface ChunkPort {
    val position: ChunkPosition
    fun section(height: SectionHeight): ChunkSectionPort?
}

/** Requires upstream ChunkManager and dimension bounds. */
interface ChunkManagerPort {
    fun chunk(position: ChunkPosition): ChunkPort?
    fun unload(position: ChunkPosition)
}

/** Requires upstream heightmap rules and block opacity. */
interface HeightmapPort {
    fun height(localX: Int, localZ: Int): Int
}
