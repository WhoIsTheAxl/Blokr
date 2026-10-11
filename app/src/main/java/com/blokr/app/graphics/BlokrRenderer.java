package com.blokr.app.graphics;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Android GLES bootstrap renderer; Minosoft rendering is not connected yet. */
public final class BlokrRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "BlokrRenderer";
    private volatile int width;
    private volatile int height;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.i(TAG, "OpenGL ES: " + GLES30.glGetString(GLES30.GL_VERSION));
        GLES30.glClearColor(0.11f, 0.08f, 0.19f, 1.0f);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        this.width = width;
        this.height = height;
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
