package com.blokr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.blokr.app.diagnostic.Diagnostics;

/** Lightweight splash and preparation stage, before opening the main launcher. */
public final class StartActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final int[] progress = {0, 18, 39, 62, 81, 100};
    private final String[] stages = {
        "Starting Blokr...",
        "Checking application resources...",
        "Preparing game packages...",
        "Initializing local services...",
        "Finalizing startup...",
        "Ready!"
    };
    private int step;
    private TextView status;
    private ProgressBar bar;
    private boolean navigated;
    private final Runnable advance = new Runnable() {
        @Override public void run() {
            if (isFinishing() || isDestroyed()) return;
            status.setText(stages[step]);
            bar.setProgress(progress[step]);
            Diagnostics.info("StartActivity", "Simulated preparation: " + progress[step] + "% - " + stages[step]);
            if (++step < stages.length) {
                handler.postDelayed(this, 650);
            } else {
                handler.postDelayed(() -> {
                    if (isFinishing() || isDestroyed() || navigated) return;
                    navigated = true;
                    Diagnostics.info("StartActivity", "Preparation complete; opening MainActivity");
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    finish();
                }, 450);
            }
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Diagnostics.info("StartActivity", "Splash created; simulated preparation only");
        int pad = (int) (28 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(0xFF18112C);

        TextView brand = new TextView(this);
        brand.setText("BLOKR");
        brand.setTextSize(42);
        brand.setTextColor(0xFFFFFFFF);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your world. Your games.");
        subtitle.setTextSize(15);
        subtitle.setTextColor(0xFFCCBDEB);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.bottomMargin = pad * 2;
        root.addView(subtitle, subtitleParams);

        status = new TextView(this);
        status.setText(stages[0]);
        status.setTextColor(0xFFFFFFFF);
        status.setTextSize(15);
        status.setGravity(Gravity.CENTER);
        root.addView(status);

        bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(0);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(-1, (int) (8 * getResources().getDisplayMetrics().density));
        barParams.topMargin = pad;
        root.addView(bar, barParams);

        TextView note = new TextView(this);
        note.setText("Preparing resources (preview)");
        note.setTextSize(12);
        note.setTextColor(0xFFB6A6D2);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(-1, -2);
        noteParams.topMargin = pad / 2;
        root.addView(note, noteParams);

        setContentView(root);
        handler.postDelayed(advance, 500);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        Diagnostics.info("StartActivity", "Destroyed; finishing=" + isFinishing());
        super.onDestroy();
    }
}
