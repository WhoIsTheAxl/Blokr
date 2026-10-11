/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.biome.accessor.noise

import de.bixilon.kutil.math.simple.IntMath.clamp
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight

abstract class NoiseBiomeAccessor(
    val world: World,
    val seed: Long = 0L,
) : BiomeAccessor {

    protected abstract fun get(x: Int, y: Int, z: Int, chunk: Chunk): Biome?

    override fun get(chunk: Chunk, position: InChunkPosition): Biome? {
        val biomeY = if (world.dimension.supports3DBiomes) position.y.clamp(world.dimension.minY, world.dimension.maxY) else 0

        val cache = chunk.sections[biomeY.sectionHeight]?.biomes

        cache?.getCached(position.inSectionPosition)?.let { return it }

        val biome = get(position.x, biomeY, position.z, chunk)

        cache?.set(position.inSectionPosition, biome)

        return biome
    }
}
