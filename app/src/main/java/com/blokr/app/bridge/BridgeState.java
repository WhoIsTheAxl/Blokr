package com.blokr.app.bridge;

/** Lifecycle state of the Minosoft runtime connection. */
public enum BridgeState {
    UNAVAILABLE,
    READY,
    RUNNING,
    PAUSED,
    STOPPED
}
