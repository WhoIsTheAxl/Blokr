package com.blokr;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import com.blokr.app.bridge.MinosoftBridge;

/**
 * Android bootstrap shell. Minosoft is not attached until its portable
 * runtime is wired into the Android module.
 */
public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView status = new TextView(this);
        status.setText("Blokr\nAndroid bootstrap ready\nMinosoft: "
            + MinosoftBridge.getInstance().getState());
        status.setTextSize(22);
        status.setGravity(Gravity.CENTER);
        setContentView(status);
    }
}
