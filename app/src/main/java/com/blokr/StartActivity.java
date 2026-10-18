package com.blokr;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.blokr.app.diagnostic.Diagnostics;

/**
 * Bedrock-inspired loading presentation. The progress animation is cosmetic:
 * it does not claim that Minecraft assets or the Minosoft runtime have loaded.
 * All artwork is drawn locally; no proprietary Minecraft font is bundled.
 */
public final class StartActivity extends Activity {
    private static final long DURATION_MS = 3900L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ValueAnimator progressAnimator;
    private boolean navigated;

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Diagnostics.info("StartActivity", "Minecraft-inspired animated splash (preview)");
        getWindow().setStatusBarColor(0xFF173A23);
        getWindow().setNavigationBarColor(0xFF10271A);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {0xFF4C9D55, 0xFF2D753E, 0xFF153F29}));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(18), dp(12), dp(18), dp(12));
        scroll.addView(root);

        PixelLogo logo = new PixelLogo();
        root.addView(logo, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(112)));

        TextView edition = new TextView(this);
        edition.setText("ANDROID EDITION");
        edition.setTextSize(13);
        edition.setTextColor(0xFFE7F5DF);
        edition.setGravity(Gravity.CENTER);
        edition.setTypeface(android.graphics.Typeface.create("monospace",
                android.graphics.Typeface.BOLD));
        edition.setLetterSpacing(0.16f);
        LinearLayout.LayoutParams editionParams = new LinearLayout.LayoutParams(-1, -2);
        editionParams.bottomMargin = dp(25);
        root.addView(edition, editionParams);

        TextView loading = new TextView(this);
        loading.setText("Loading Blokr...");
        loading.setTextSize(16);
        loading.setTextColor(Color.WHITE);
        loading.setGravity(Gravity.CENTER);
        loading.setTypeface(android.graphics.Typeface.create("monospace",
                android.graphics.Typeface.BOLD));
        root.addView(loading);

        BlockProgress progress = new BlockProgress();
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                Math.min(dp(370), getResources().getDisplayMetrics().widthPixels - dp(54)),
                dp(24));
        barParams.gravity = Gravity.CENTER_HORIZONTAL;
        barParams.topMargin = dp(12);
        root.addView(progress, barParams);

        TextView note = new TextView(this);
        note.setText("Launcher preview  •  Engine initialization not started");
        note.setTextSize(11);
        note.setTextColor(0xFFDBECDD);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(-1, -2);
        noteParams.topMargin = dp(11);
        root.addView(note, noteParams);
        setContentView(scroll);

        progressAnimator = ValueAnimator.ofFloat(0f, 1f);
        progressAnimator.setDuration(DURATION_MS);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            progress.setFraction(fraction);
            if (fraction < 0.28f) loading.setText("Loading Blokr...");
            else if (fraction < 0.72f) loading.setText("Preparing launcher...");
            else loading.setText("Almost ready...");
        });
        progressAnimator.start();
        handler.postDelayed(() -> {
            if (navigated || isFinishing() || isDestroyed()) return;
            navigated = true;
            Diagnostics.info("StartActivity", "Preview animation finished; opening launcher");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, DURATION_MS + 200);
    }

    /** Custom block lettering for BLOKR: original 5x7 pixel glyphs, no font asset. */
    private final class PixelLogo extends View {
        private final Paint paint = new Paint();
        private final String[][] glyphs = {
                {"11110","10001","10001","11110","10001","10001","11110"}, // B
                {"10000","10000","10000","10000","10000","10000","11111"}, // L
                {"01110","10001","10001","10001","10001","10001","01110"}, // O
                {"10001","10010","10100","11000","10100","10010","10001"}, // K
                {"11110","10001","10001","11110","10100","10010","10001"}  // R
        };
        PixelLogo() { super(StartActivity.this); setContentDescription("BLOKR"); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float unit = Math.min((getWidth() - dp(12)) / 33f, (getHeight() - dp(16)) / 9f);
            if (unit <= 0) return;
            float x0 = (getWidth() - 33f * unit) / 2f;
            float y0 = (getHeight() - 7f * unit) / 2f;
            for (int letter = 0; letter < glyphs.length; letter++) {
                for (int row = 0; row < 7; row++) {
                    for (int col = 0; col < 5; col++) {
                        if (glyphs[letter][row].charAt(col) != '1') continue;
                        float x = x0 + (letter * 7 + col) * unit;
                        float y = y0 + row * unit;
                        paint.setColor(0xFF15331C);
                        canvas.drawRect(x + unit * .22f, y + unit * .32f,
                                x + unit * 1.22f, y + unit * 1.32f, paint);
                        paint.setColor(0xFFF5F5F0);
                        canvas.drawRect(x, y, x + unit, y + unit, paint);
                        paint.setColor(0xFFC4D2BE);
                        canvas.drawRect(x, y + unit * .79f, x + unit,
                                y + unit, paint);
                    }
                }
            }
        }
    }

    /** Pixel-edged loading track with discrete animated green segments. */
    private final class BlockProgress extends View {
        private final Paint paint = new Paint();
        private float fraction;
        BlockProgress() { super(StartActivity.this); setContentDescription("Launcher loading preview"); }
        void setFraction(float value) {
            fraction = Math.max(0f, Math.min(1f, value));
            invalidate();
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth(), h = getHeight();
            paint.setColor(0xFF10251A);
            canvas.drawRect(0, 0, w, h, paint);
            paint.setColor(0xFFB5C6B5);
            canvas.drawRect(dp(2), dp(2), w - dp(2), h - dp(2), paint);
            paint.setColor(0xFF304936);
            canvas.drawRect(dp(4), dp(4), w - dp(4), h - dp(4), paint);
            float innerLeft = dp(6), innerWidth = Math.max(0, w - dp(12));
            int segments = 32;
            float segmentWidth = innerWidth / segments;
            int filled = Math.round(fraction * segments);
            for (int i = 0; i < filled; i++) {
                float x = innerLeft + i * segmentWidth;
                paint.setColor(i % 2 == 0 ? 0xFF8BD45C : 0xFF78C34B);
                canvas.drawRect(x, dp(6), x + Math.max(1, segmentWidth - dp(1)),
                        h - dp(6), paint);
            }
        }
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator = null;
        }
        super.onDestroy();
    }
}
