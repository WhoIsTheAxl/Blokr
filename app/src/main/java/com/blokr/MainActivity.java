package com.blokr;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Android bootstrap shell. Minosoft runtime integration follows in later milestones.
 */
public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView status = new TextView(this);
        status.setText("Blokr\nAndroid bootstrap ready");
        status.setTextSize(22);
        status.setGravity(Gravity.CENTER);
        setContentView(status);
    }
}
