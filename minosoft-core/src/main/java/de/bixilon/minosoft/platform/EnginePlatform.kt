package de.bixilon.minosoft.platform

import java.io.File

/**
 * Minimal platform boundary for the Android port.
 * Upstream gameplay code should depend on this interface, not Activity or desktop APIs.
 * This interface does not imply a complete Minosoft engine is present.
 */
interface EnginePlatform {
    fun dataDirectory(): File
    fun monotonicTimeNanos(): Long
    fun log(level: LogLevel, tag: String, message: String, failure: Throwable? = null)
}

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

/** Explicit installation prevents accidental use before Android bootstrapping. */
class EngineEnvironment(private val platform: EnginePlatform) {
    init {
        require(platform.dataDirectory().isDirectory || platform.dataDirectory().mkdirs()) {
            "Cannot create engine data directory"
        }
    }
    fun platform(): EnginePlatform = platform
}
