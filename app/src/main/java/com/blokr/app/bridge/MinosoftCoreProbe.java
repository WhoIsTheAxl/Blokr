package com.blokr.app.bridge;

import de.bixilon.minosoft.gui.rendering.RenderingStates;
import de.bixilon.minosoft.data.world.portable.PortableWorld;
import de.bixilon.minosoft.data.world.chunk.ChunkSize;

/** First real Minosoft source compatibility check on Android. */
public final class MinosoftCoreProbe {
    private MinosoftCoreProbe() {}

    public static String check() {
        RenderingStates state = RenderingStates.LOADING;
        return "Minosoft Kotlin: " + state.name()
            + " (active=" + state.getActive() + ")";
    }

    public static String checkWorldState() {
        PortableWorld world = new PortableWorld();
        world.setBlock(-1, 64, -17, 7);
        world.setBlock(16, -64, 16, 3);
        if (world.getBlock(-1, 64, -17) != 7
                || world.getBlock(16, -64, 16) != 3
                || world.getBlock(-1, 64, -16) != 0
                || world.loadedSections() != 2
                || ChunkSize.BLOCKS_PER_SECTION != 4096) {
            throw new IllegalStateException("World-state storage validation failed");
        }
        world.tick();
        if (world.getTickCount() != 1L) throw new IllegalStateException("Tick failed");
        world.setBlock(-1, 64, -17, 0);
        if (world.loadedSections() != 1) throw new IllegalStateException("Section cleanup failed");
        return "Portable world state: PASS (negative coordinates, sections, tick)";
    }
}
