package com.blokr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blokr.app.bridge.MinosoftBridge;
import com.blokr.app.view.activities.GameActivity;

/** Android launcher for the bridge and graphics bootstrap. */
public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        TextView status = new TextView(this);
        status.setText("Blokr\nAndroid bootstrap ready\nMinosoft: "
            + MinosoftBridge.getInstance().getState());
        status.setTextSize(22);
        status.setGravity(Gravity.CENTER);
        layout.addView(status);

        Button graphics = new Button(this);
        graphics.setText("Test OpenGL ES 3.0");
        graphics.setOnClickListener(v ->
            startActivity(new Intent(this, GameActivity.class)));
        layout.addView(graphics);
        setContentView(layout);
    }
}
