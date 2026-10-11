package com.blokr.app.view.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.blokr.app.graphics.BlokrSurfaceView;

/** Graphics bootstrap activity. No Minosoft session is started yet. */
public final class GameActivity extends Activity {
    private BlokrSurfaceView surface;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (getPackageManager().hasSystemFeature("android.hardware.opengles.aep")) {
            // AEP is optional; GLES 3.0 remains the actual requirement.
        }
        surface = new BlokrSurfaceView(this);
        setContentView(surface);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (surface != null) surface.onResume();
    }

    @Override
    protected void onPause() {
        if (surface != null) surface.onPause();
        super.onPause();
    }
}
