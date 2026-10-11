# Minosoft core (Android-compatible staging module)

This module is the first extraction point for **original Minosoft sources** that
can compile on Android without desktop GLFW, LWJGL or JavaFX.

The first integrated class is `RenderingStates.kt`, copied verbatim from
`src/main/java/de/bixilon/minosoft/gui/rendering/RenderingStates.kt`.
Its original copyright and GPL header are retained.

This is **not yet the complete Minosoft core**: networking, sessions, world
state and the rendering backend remain to be ported after dependency analysis.
Do not introduce duplicate implementations of Minosoft systems here.
