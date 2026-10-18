package com.blokr.app.view.activities;

import android.app.Activity;
import android.os.Bundle;

import com.blokr.app.graphics.BlokrSurfaceView;
import com.blokr.app.diagnostic.Diagnostics;

/** GLES terrain preview backed by Minosoft-core PortableWorld data. */
public final class GameActivity extends Activity {
    private BlokrSurfaceView surface;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Diagnostics.info("GameActivity", "Creating GLES PortableWorld terrain preview");
        if (getPackageManager().hasSystemFeature("android.hardware.opengles.aep")) {
            // AEP is optional; GLES 3.0 remains the actual requirement.
        }
        surface = new BlokrSurfaceView(this);
        setContentView(surface);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Diagnostics.info("GameActivity", "Resume surface");
        if (surface != null) surface.onResume();
    }

    @Override
    protected void onPause() {
        Diagnostics.info("GameActivity", "Pause surface");
        if (surface != null) surface.onPause();
        super.onPause();
    }
}
