package com.blokr.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.blokr.app.bridge.MinosoftBridge;
import com.blokr.app.bridge.NetworkTransportProbe;
import com.blokr.app.diagnostic.Diagnostics;
import com.blokr.app.view.activities.GameActivity;

/**
 * Minecraft Java-inspired launcher, built entirely with Android views.
 * Actions are clearly labelled as previews until the game runtime exists.
 */
public final class MinecraftLauncherView {
    private static final int WHITE = 0xFFF7F7F7;
    private static final int GREY = 0xFFBDBDBD;
    private static final int PANEL = 0xE0191919;
    private static final int BORDER = 0xFF101010;
    private final Activity activity;
    private final UIFactory factory;
    private final TextView diagnostics;

    public MinecraftLauncherView(Activity activity) {
        this.activity = activity;
        factory = new UIFactory(activity);
        diagnostics = label("Engine diagnostics: pending...", 11, GREY);
        diagnostics.setGravity(Gravity.CENTER);
    }

    public TextView diagnosticsView() { return diagnostics; }

    private TextView label(String text, int size, int color) {
        TextView view = new TextView(activity);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        view.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        return view;
    }

    private GradientDrawable box(int fill, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setStroke(factory.dp(2), stroke);
        drawable.setCornerRadius(0);
        return drawable;
    }

    private Button menuButton(String text, View.OnClickListener listener) {
        Button button = new Button(activity);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setTextColor(WHITE);
        button.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        button.setBackground(box(0xFF777777, BORDER));
        button.setOnClickListener(listener);
        button.setMinHeight(factory.dp(42));
        button.setContentDescription(text);
        return button;
    }

    private void addButton(LinearLayout parent, Button button) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, factory.dp(44));
        params.topMargin = factory.dp(7);
        parent.addView(button, params);
    }

    public View create() {
        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        scroll.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[] {0xFF263C31, 0xFF182B29, 0xFF101B1B}));
        LinearLayout outer = new LinearLayout(activity);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setGravity(Gravity.CENTER);
        outer.setPadding(factory.dp(14), factory.dp(12), factory.dp(14), factory.dp(12));
        scroll.addView(outer);

        LinearLayout panel = new LinearLayout(activity);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        panel.setPadding(factory.dp(18), factory.dp(14), factory.dp(18), factory.dp(12));
        panel.setBackground(box(PANEL, 0xFF5E5E5E));
        LinearLayout.LayoutParams panelParams = new LinearLayout.LayoutParams(
                Math.min(factory.dp(420), activity.getResources().getDisplayMetrics().widthPixels - factory.dp(28)),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        panelParams.gravity = Gravity.CENTER;
        outer.addView(panel, panelParams);

        TextView title = label("B L O K R", 36, WHITE);
        title.setShadowLayer(factory.dp(3), factory.dp(3), factory.dp(4), Color.BLACK);
        panel.addView(title);
        TextView subtitle = label("JAVA EDITION  •  ANDROID", 11, 0xFFFFE58C);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.bottomMargin = factory.dp(12);
        panel.addView(subtitle, subtitleParams);

        addButton(panel, menuButton("Singleplayer", v -> new AlertDialog.Builder(activity)
                .setTitle("Singleplayer")
                .setMessage("World loading is not available yet. Minosoft game runtime integration is still in progress.")
                .setPositiveButton("OK", null).show()));
        addButton(panel, menuButton("Multiplayer", v -> new AlertDialog.Builder(activity)
                .setTitle("Multiplayer")
                .setMessage("Minecraft multiplayer is not connected yet. You can run a TCP-only connectivity test in Diagnostics.")
                .setPositiveButton("OK", null).show()));
        addButton(panel, menuButton("Graphics Preview", v ->
                activity.startActivity(new Intent(activity, GameActivity.class))));

        LinearLayout bottom = new LinearLayout(activity);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(-1, factory.dp(44));
        bottomParams.topMargin = factory.dp(7);
        panel.addView(bottom, bottomParams);
        Button options = menuButton("Options...", v -> new AlertDialog.Builder(activity)
                .setTitle("Blokr Options")
                .setMessage("OpenGL ES 3.0 preview is available. Game settings will become available when Minosoft is integrated.")
                .setPositiveButton("OK", null).show());
        Button diagnosticButton = menuButton("Diagnostics", v -> showDiagnostics());
        LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(0, -1, 1);
        left.rightMargin = factory.dp(5);
        bottom.addView(options, left);
        LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(0, -1, 1);
        right.leftMargin = factory.dp(5);
        bottom.addView(diagnosticButton, right);

        TextView status = label("Minosoft: " + MinosoftBridge.getInstance().getState()
                + "  |  Android preview", 11, GREY);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.topMargin = factory.dp(10);
        panel.addView(status, statusParams);

        LinearLayout.LayoutParams diagnosticsParams = new LinearLayout.LayoutParams(-1, -2);
        diagnosticsParams.topMargin = factory.dp(6);
        panel.addView(diagnostics, diagnosticsParams);
        TextView footer = label("Blokr is an independent project • Not affiliated with Mojang", 10, GREY);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(-1, -2);
        footerParams.topMargin = factory.dp(10);
        outer.addView(footer, footerParams);
        return scroll;
    }

    private void showDiagnostics() {
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(factory.dp(20), factory.dp(8), factory.dp(20), 0);
        EditText host = new EditText(activity);
        host.setSingleLine(true);
        host.setHint("Server hostname");
        content.addView(host);
        EditText port = new EditText(activity);
        port.setSingleLine(true);
        port.setInputType(InputType.TYPE_CLASS_NUMBER);
        port.setText("25565");
        content.addView(port);
        TextView result = label("TCP test only — no Minecraft handshake", 12, 0xFF333333);
        content.addView(result);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Network Diagnostics")
                .setView(content)
                .setNegativeButton("Close", null)
                .setPositiveButton("Test TCP", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String target = host.getText().toString().trim();
            int targetPort;
            try {
                targetPort = Integer.parseInt(port.getText().toString().trim());
                if (targetPort < 1 || targetPort > 65535 || target.isEmpty())
                    throw new IllegalArgumentException();
            } catch (RuntimeException invalid) {
                result.setText("Enter a valid hostname and port (1–65535)");
                return;
            }
            result.setText("Connecting...");
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            final int selectedPort = targetPort;
            new Thread(() -> {
                String message;
                try {
                    message = NetworkTransportProbe.connect(target, selectedPort, 5000);
                } catch (Exception failure) {
                    message = "TCP failed: " + failure.getClass().getSimpleName();
                    Diagnostics.warn("NetworkProbe", message);
                }
                final String output = message;
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (activity.isFinishing() || activity.isDestroyed() || !dialog.isShowing()) return;
                    result.setText(output);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                });
            }, "Blokr-TCP-Probe").start();
        }));
        dialog.show();
    }
}
