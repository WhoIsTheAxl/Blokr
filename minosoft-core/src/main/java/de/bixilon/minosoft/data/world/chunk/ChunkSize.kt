/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * GPL-3.0-or-later. Ported unchanged from Bixilon/Minosoft:
 * src/main/java/de/bixilon/minosoft/data/world/chunk/ChunkSize.kt
 */
package de.bixilon.minosoft.data.world.chunk

object ChunkSize {
    const val SECTION_LENGTH = 16
    const val SECTION_WIDTH_X = SECTION_LENGTH
    const val SECTION_MAX_X = SECTION_WIDTH_X - 1
    const val SECTION_WIDTH_Z = SECTION_LENGTH
    const val SECTION_MAX_Z = SECTION_WIDTH_Z - 1
    const val SECTION_HEIGHT_Y = SECTION_LENGTH
    const val SECTION_MAX_Y = SECTION_HEIGHT_Y - 1
    const val BLOCKS_PER_SECTION = SECTION_WIDTH_X * SECTION_HEIGHT_Y * SECTION_WIDTH_X

    const val CHUNK_MIN_Y = -2048
    const val CHUNK_MIN_SECTION = CHUNK_MIN_Y / SECTION_HEIGHT_Y
    const val CHUNK_MAX_Y = 2048
    const val CHUNK_MAX_SECTION = CHUNK_MAX_Y / SECTION_HEIGHT_Y
    const val CHUNK_MAX_HEIGHT = CHUNK_MAX_Y - CHUNK_MIN_Y
    const val CHUNK_MAX_SECTIONS = CHUNK_MAX_HEIGHT / SECTION_HEIGHT_Y

    @Deprecated("cherry pick rewrite-light-engine")
    const val MAX_LIGHT_LEVEL_I = 15
}
