package com.blokr.app.graphics;

import android.content.Context;
import android.opengl.GLSurfaceView;

/** Owns the Android EGL surface and the GLES 3.0 renderer lifecycle. */
public final class BlokrSurfaceView extends GLSurfaceView {
    private final BlokrRenderer renderer;

    public BlokrSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(3);
        setEGLConfigChooser(8, 8, 8, 8, 24, 8);
        renderer = new BlokrRenderer();
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public BlokrRenderer getBlokrRenderer() {
        return renderer;
    }
}
