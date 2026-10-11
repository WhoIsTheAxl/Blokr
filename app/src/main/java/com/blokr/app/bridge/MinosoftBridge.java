package com.blokr.app.bridge;

import java.util.Objects;

/**
 * Thread-safe lifecycle gateway between Blokr and a Minosoft runtime.
 *
 * Registration is explicit: a runtime must be attached before any lifecycle
 * operation. The Android bootstrap does not silently start a fake engine.
 */
public final class MinosoftBridge {
    private static final MinosoftBridge INSTANCE = new MinosoftBridge();

    private MinosoftRuntime runtime;
    private BridgeState state = BridgeState.UNAVAILABLE;

    private MinosoftBridge() {}

    public static MinosoftBridge getInstance() {
        return INSTANCE;
    }

    public synchronized BridgeState getState() {
        return state;
    }

    /**
     * Returns the attached graphics integration, if one exists.
     * No desktop Minosoft classes are loaded by this boundary.
     */
    public synchronized MinosoftGraphicsRuntime graphics() {
        return runtime instanceof MinosoftGraphicsRuntime
            ? (MinosoftGraphicsRuntime) runtime : null;
    }

    public synchronized boolean isAvailable() {
        return runtime != null;
    }

    /** Attach the platform-compatible Minosoft implementation before start(). */
    public synchronized void attach(MinosoftRuntime implementation) {
        Objects.requireNonNull(implementation, "implementation");
        if (runtime != null) {
            throw new IllegalStateException("A Minosoft runtime is already attached");
        }
        runtime = implementation;
        state = BridgeState.READY;
    }

    public synchronized void start() {
        requireState(BridgeState.READY);
        runtime.start();
        state = BridgeState.RUNNING;
    }

    public synchronized void pause() {
        requireState(BridgeState.RUNNING);
        runtime.pause();
        state = BridgeState.PAUSED;
    }

    public synchronized void resume() {
        requireState(BridgeState.PAUSED);
        runtime.resume();
        state = BridgeState.RUNNING;
    }

    public synchronized void stop() {
        if (runtime == null) {
            return;
        }
        if (state == BridgeState.RUNNING || state == BridgeState.PAUSED) {
            runtime.stop();
        }
        state = BridgeState.STOPPED;
    }

    /** Detach only after the runtime has stopped or before it starts. */
    public synchronized void detach() {
        if (state == BridgeState.RUNNING || state == BridgeState.PAUSED) {
            throw new IllegalStateException("Stop the runtime before detaching");
        }
        runtime = null;
        state = BridgeState.UNAVAILABLE;
    }

    private void requireState(BridgeState expected) {
        if (runtime == null || state != expected) {
            throw new IllegalStateException(
                "Minosoft runtime must be " + expected + " (currently " + state + ")"
            );
        }
    }
}
