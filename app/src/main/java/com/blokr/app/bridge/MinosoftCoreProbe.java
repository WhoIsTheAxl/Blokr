package com.blokr.app.bridge;

import de.bixilon.minosoft.gui.rendering.RenderingStates;

/** First real Minosoft source compatibility check on Android. */
public final class MinosoftCoreProbe {
    private MinosoftCoreProbe() {}

    public static String check() {
        RenderingStates state = RenderingStates.LOADING;
        return "Minosoft Kotlin: " + state.name()
            + " (active=" + state.getActive() + ")";
    }
}
