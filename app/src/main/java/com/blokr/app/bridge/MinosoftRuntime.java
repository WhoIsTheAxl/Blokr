package com.blokr.app.bridge;

/**
 * Implementation contract for a compatible Minosoft runtime.
 *
 * The implementation will directly invoke Minosoft APIs once its portable
 * classes are available to the Android module. No desktop classes are loaded
 * by this API itself.
 */
public interface MinosoftRuntime {
    void start();
    void pause();
    void resume();
    void stop();
}
