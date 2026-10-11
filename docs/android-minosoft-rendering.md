# Minosoft Android renderer integration notes

This is a **port of Minosoft's rendering backend**, not a new Minecraft renderer.

## Verified source interfaces

- `src/main/java/de/bixilon/minosoft/gui/rendering/RenderContext.kt` constructs a `Window` through `WindowFactory.factory?.create(this)` and a `RenderSystem` through `RenderSystemFactory.factory?.create(this)`. It then constructs the existing camera, shader, framebuffer, model, texture and renderer managers.
- `system/window/WindowFactory.kt` registers the desktop GLFW factory by default. `Window` defines begin/end, event polling, cursor/window state, swap interval, etc. Android's implementation will need surface-based equivalents or explicitly unsupported operations.
- `system/base/RenderSystemFactory.kt` registers the LWJGL OpenGL factory by default. Android must install a GLES-compatible factory **before** creating `RenderContext`.
- `system/base/RenderSystem.kt` owns shader management, vertex/uniform buffers, textures, framebuffer, queries, viewport and render state. Implementing this interface is the backend port, not reimplementing world/chunk/entity rendering.
- `system/opengl/OpenGLRenderSystemFactory.kt` references `org.lwjgl.opengl.GL`. `system/window/glfw/GlfwWindowFactory.kt` references `org.lwjgl.glfw.GLFW`. These implementations cannot be used directly on Android.

## Android seam implemented

`MinosoftBridge` can attach a `MinosoftRuntime`. A runtime that also implements `MinosoftGraphicsRuntime` receives GLES surface creation, resize, and per-frame callbacks on the GL thread. The Android `BlokrRenderer` does **not** draw Minecraft content. It only forwards events, or clears a placeholder screen while the runtime is unavailable.

## Next dependencies / blockers

1. Extract/build an Android-compatible Minosoft runtime and its transitive Kotlin/JVM dependencies; the current `:app` module does **not** compile Minosoft source.
2. Provide a Minosoft `WindowFactory` implementation backed by the Android surface and Android input/lifecycle.
3. Provide a Minosoft `RenderSystemFactory` implementation using GLES. Keep Minosoft's existing renderer managers and rendering pipeline.
4. Audit unsupported desktop OpenGL features (notably geometry shaders, polygon mode, and GLSL 330 syntax) and introduce GLES-specific fallbacks only where necessary.
5. Reconcile Minosoft's own render thread/window begin-end loop with `GLSurfaceView`'s GL thread. Do not invoke Minosoft rendering from two threads.
6. Handle Android EGL context loss and surface recreation without treating it as a Minecraft session termination.

No Minosoft classes are directly called from the Android APK yet. Do not describe the current adapter as a completed engine port.
