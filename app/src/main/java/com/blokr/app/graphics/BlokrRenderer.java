package com.blokr.app.graphics;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.blokr.app.bridge.BridgeState;
import com.blokr.app.bridge.MinosoftBridge;
import com.blokr.app.bridge.MinosoftGraphicsRuntime;
import com.blokr.app.diagnostic.Diagnostics;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * EGL lifecycle adapter only. Minosoft owns all game rendering.
 * Until a graphics runtime is attached, renders a local PortableWorld demo.
 */
public final class BlokrRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "BlokrRenderer";
    private final PortableWorldRenderer worldPreview = new PortableWorldRenderer();
    private volatile int width;
    private volatile int height;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Diagnostics.info(TAG, "Surface created; OpenGL ES: " + GLES30.glGetString(GLES30.GL_VERSION));
        GLES30.glClearColor(0.11f, 0.08f, 0.19f, 1.0f);
        MinosoftGraphicsRuntime graphics = MinosoftBridge.getInstance().graphics();
        if (graphics != null) graphics.onSurfaceCreated();
        else worldPreview.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Diagnostics.info(TAG, "Surface resized to " + width + "x" + height);
        this.width = width;
        this.height = height;
        GLES30.glViewport(0, 0, width, height);
        MinosoftGraphicsRuntime graphics = MinosoftBridge.getInstance().graphics();
        if (graphics != null) graphics.onSurfaceChanged(width, height);
        else worldPreview.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        MinosoftBridge bridge = MinosoftBridge.getInstance();
        MinosoftGraphicsRuntime graphics = bridge.graphics();
        if (graphics != null && bridge.getState() == BridgeState.RUNNING) {
            graphics.renderFrame();
        } else {
            worldPreview.renderFrame();
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
