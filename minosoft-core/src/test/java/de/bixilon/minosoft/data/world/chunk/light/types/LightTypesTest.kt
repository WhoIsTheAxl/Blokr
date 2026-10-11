package de.bixilon.minosoft.data.world.chunk.light.types

import org.junit.Assert.*
import org.junit.Test

class LightTypesTest {
    @Test fun everyLightPairRoundTrips() {
        for (block in 0..15) for (sky in 0..15) {
            val level = LightLevel(block, sky)
            assertEquals(block, level.block)
            assertEquals(sky, level.sky)
            assertEquals((sky shl 4) or block, level.index)
            assertEquals(2, level.toString().length)
        }
    }

    @Test fun lightMaxAndWithPreserveIndependentChannels() {
        val a = LightLevel(12, 3)
        val b = LightLevel(2, 14)
        assertEquals(LightLevel(12, 14), a.max(b))
        assertEquals(LightLevel(12, 9), a.with(sky = 9))
        assertEquals(0, LightLevel.EMPTY.index)
        assertEquals(255, LightLevel.MAX.index)
    }

    @Test fun invalidLightValuesAreRejected() {
        for (invalid in listOf(-1, 16, 100)) {
            try {
                LightLevel(invalid, 0)
                fail("Invalid block light accepted")
            } catch (_: IllegalArgumentException) {}
            try {
                LightLevel(0, invalid)
                fail("Invalid sky light accepted")
            } catch (_: IllegalArgumentException) {}
        }
    }

    @Test fun lightArrayStorageAndClear() {
        val lights = LightArray()
        assertEquals(4096, lights.array.size)
        lights[0] = LightLevel(1, 15)
        lights[4095] = LightLevel(15, 1)
        assertEquals(LightLevel(1, 15), lights[0])
        assertEquals(LightLevel(15, 1), lights[4095])
        lights.clear()
        assertEquals(LightLevel.EMPTY, lights[0])
        assertEquals(LightLevel.EMPTY, lights[4095])
    }
}
