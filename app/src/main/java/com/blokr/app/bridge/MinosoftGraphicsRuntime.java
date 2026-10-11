package com.blokr.app.bridge;

/**
 * Optional graphics contract implemented by the Android-compatible Minosoft
 * integration. Called only on the GLSurfaceView GL thread.
 *
 * Implementations must own Minecraft rendering; Blokr only forwards EGL
 * surface events and frame scheduling.
 */
public interface MinosoftGraphicsRuntime {
    void onSurfaceCreated();
    void onSurfaceChanged(int width, int height);
    void renderFrame();
}
