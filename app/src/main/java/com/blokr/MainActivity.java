package com.blokr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.os.SystemClock;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.view.View;
import com.blokr.app.bridge.NetworkTransportProbe;

import com.blokr.app.bridge.MinosoftBridge;
import com.blokr.app.bridge.MinosoftCoreProbe;
import com.blokr.app.diagnostic.Diagnostics;
import de.bixilon.minosoft.protocol.AndroidProtocolProbe;
import de.bixilon.minosoft.protocol.protocol.buffers.AndroidPrimitiveBufferProbe;
import de.bixilon.minosoft.protocol.protocol.buffers.KUtilBufferProbe;
import de.bixilon.minosoft.protocol.network.AndroidPacketFraming;
import de.bixilon.minosoft.protocol.network.AndroidNettyFramingProbe;
import com.blokr.app.view.activities.GameActivity;

/** Android launcher for the bridge and graphics bootstrap. */
public final class MainActivity extends Activity {
    private final Handler diagnosticHandler = new Handler(Looper.getMainLooper());
    private final Runnable heartbeat = new Runnable() {
        @Override public void run() {
            Diagnostics.info("LauncherHeartbeat", "alive; pid=" + Process.myPid()
                + "; finishing=" + isFinishing());
            diagnosticHandler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Diagnostics.info("MainActivity", "Opened from StartActivity; bridge=" + MinosoftBridge.getInstance().getState());
        Intent launchIntent = getIntent();
        Diagnostics.info("LaunchPath", "pid=" + Process.myPid()
            + "; taskId=" + getTaskId()
            + "; savedState=" + (savedInstanceState != null)
            + "; action=" + (launchIntent == null ? "null" : launchIntent.getAction())
            + "; flags=" + (launchIntent == null ? "null" : Integer.toHexString(launchIntent.getFlags())));
        Diagnostics.info("StartupStage", "01 creating launcher layout");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.blokr_logo);
        int logoSize = (int) (96 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
        logoParams.gravity = Gravity.CENTER_HORIZONTAL;
        logoParams.bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);
        layout.addView(logo, logoParams);

        TextView status = new TextView(this);
        status.setText("Blokr\nAndroid bootstrap ready\nMinosoft: "
            + MinosoftBridge.getInstance().getState()
            + "\nProtocol diagnostics: running in background...");
        Diagnostics.info("StartupStage", "02 status view initialized; probes deferred");
        status.setTextSize(22);
        status.setGravity(Gravity.CENTER);
        layout.addView(status);

        Button graphics = new Button(this);
        graphics.setText("Test OpenGL ES 3.0");
        graphics.setOnClickListener(v ->
            startActivity(new Intent(this, GameActivity.class)));
        layout.addView(graphics);
        EditText host = new EditText(this);
        host.setSingleLine(true);
        host.setHint("Server hostname");
        layout.addView(host);

        EditText port = new EditText(this);
        port.setSingleLine(true);
        port.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        port.setText("25565");
        layout.addView(port);

        TextView networkStatus = new TextView(this);
        networkStatus.setText("TCP diagnostic only — no Minecraft handshake");
        layout.addView(networkStatus);

        Button connect = new Button(this);
        connect.setText("Test TCP Connection");
        connect.setOnClickListener(v -> {
            final String target = host.getText().toString().trim();
            final String portText = port.getText().toString().trim();
            final int targetPort;
            try {
                targetPort = Integer.parseInt(portText);
                if (targetPort < 1 || targetPort > 65535) throw new NumberFormatException();
                if (target.isEmpty()) throw new IllegalArgumentException("Enter a hostname");
            } catch (RuntimeException invalid) {
                networkStatus.setText("Enter a valid hostname and port (1–65535)");
                return;
            }
            connect.setEnabled(false);
            networkStatus.setText("Connecting...");
            Handler main = new Handler(Looper.getMainLooper());
            new Thread(() -> {
                String result;
                try {
                    result = NetworkTransportProbe.connect(target, targetPort, 5000);
                    Diagnostics.info("NetworkProbe", "TCP connection succeeded; port=" + targetPort);
                } catch (Exception failure) {
                    result = "TCP connection failed: " + failure.getClass().getSimpleName();
                    Diagnostics.warn("NetworkProbe", result);
                }
                final String display = result;
                main.post(() -> {
                    networkStatus.setText(display);
                    connect.setEnabled(true);
                });
            }, "Blokr-TCP-Probe").start();
        });
        layout.addView(connect);
        Diagnostics.info("StartupStage", "03 controls created; attaching scroll container");
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(layout);
        Diagnostics.info("StartupStage", "04 before setContentView");
        setContentView(scroll);
        Diagnostics.info("StartupStage", "05 after setContentView");
        scroll.getViewTreeObserver().addOnPreDrawListener(new android.view.ViewTreeObserver.OnPreDrawListener() {
            private boolean first = true;
            @Override public boolean onPreDraw() {
                if (first) {
                    first = false;
                    Diagnostics.info("RenderStage", "first pre-draw; attached=" + scroll.isAttachedToWindow());
                    scroll.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
        scroll.post(() -> Diagnostics.info("RenderStage", "view posted callback; shown=" + scroll.isShown()));
        Diagnostics.info("StartupStage", "Protocol diagnostics scheduled for +5000ms");
        diagnosticHandler.postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                Diagnostics.info("StartupStage", "Protocol diagnostics starting after 5 seconds");
                startProtocolDiagnostics(status);
            } else {
                Diagnostics.warn("StartupStage", "Protocol diagnostics skipped: activity finishing/destroyed");
            }
        }, 5000);
    }

    private void startProtocolDiagnostics(TextView status) {
        Diagnostics.info("StartupStage", "06 scheduling background protocol diagnostics");
        new Thread(() -> {
            long started = SystemClock.elapsedRealtime();
            Diagnostics.info("StartupStage", "07 protocol diagnostics worker started");
            StringBuilder report = new StringBuilder("Blokr\nAndroid bootstrap ready\nMinosoft: ")
                .append(MinosoftBridge.getInstance().getState());
            try {
                Diagnostics.info("CoreIntegration", "Checking original Minosoft Kotlin class linkage");
                String result = MinosoftCoreProbe.check();
                Diagnostics.info("CoreIntegration", result);
                report.append("\n").append(result);
            } catch (Throwable failure) {
                Diagnostics.error("CoreIntegration", "Minosoft core linkage failed", failure);
                report.append("\nMinosoft core linkage: FAIL");
            }
            try {
                Diagnostics.info("ProbeStage", "01 protocol begin");
                String result = AndroidProtocolProbe.check();
                Diagnostics.info("MinosoftProtocol", result);
                report.append("\n").append(result);
            } catch (Throwable failure) {
                Diagnostics.error("MinosoftProtocol", "Protocol probe failed", failure);
                report.append("\nProtocol: FAIL");
            }
            try {
                Diagnostics.info("ProbeStage", "02 framing begin");
                String result = AndroidPacketFraming.selfTest();
                Diagnostics.info("PacketFraming", result);
                report.append("\n").append(result);
            } catch (Throwable failure) {
                Diagnostics.error("PacketFraming", "Framing probe failed", failure);
                report.append("\nPacket framing: FAIL");
            }
            try {
                Diagnostics.info("ProbeStage", "03 Netty begin");
                String result = AndroidNettyFramingProbe.check();
                Diagnostics.info("NettyPipeline", result);
                report.append("\n").append(result);
            } catch (Throwable failure) {
                Diagnostics.error("NettyPipeline", "Netty probe failed", failure);
                report.append("\nNetty pipeline: FAIL");
            }
            try {
                Diagnostics.info("ProbeStage", "04 primitive begin");
                String result = AndroidPrimitiveBufferProbe.check();
                Diagnostics.info("PrimitiveBuffers", result);
                report.append("\n").append(result);
            } catch (Throwable failure) {
                Diagnostics.error("PrimitiveBuffers", "Primitive probe failed", failure);
                report.append("\nPrimitive buffers: FAIL");
            }
            try {
                Diagnostics.info("ProbeStage", "05 KUtil begin");
                String result = KUtilBufferProbe.check();
                Diagnostics.info("KUtilBuffers", result);
                report.append("\n").append(result);
            } catch (Throwable failure) {
                Diagnostics.error("KUtilBuffers", "KUtil probe failed", failure);
                report.append("\nKUtil buffers: FAIL");
            }
            Diagnostics.info("ProbeStage", "06 all probes completed; elapsedMs="
                + (SystemClock.elapsedRealtime() - started));
            String display = report.toString();
            runOnUiThread(() -> {
                Diagnostics.info("StartupStage", "08 updating diagnostics UI");
                if (!isFinishing() && !isDestroyed()) status.setText(display);
                Diagnostics.info("StartupStage", "09 diagnostics UI update completed");
            });
        }, "Blokr-Protocol-Diagnostics").start();
    }


    private void traceTermination(String method) {
        Diagnostics.warn("ActivityTermination", method + "; pid=" + Process.myPid()
            + "; task=" + getTaskId() + "; isTaskRoot=" + isTaskRoot()
            + "; stack=" + Log.getStackTraceString(new Throwable("Termination call site")));
    }

    @Override public void finish() {
        traceTermination("finish()");
        super.finish();
    }

    @Override public void finishAffinity() {
        traceTermination("finishAffinity()");
        super.finishAffinity();
    }

    @Override public void finishAndRemoveTask() {
        traceTermination("finishAndRemoveTask()");
        super.finishAndRemoveTask();
    }

    @Override public void onBackPressed() {
        Diagnostics.info("ActivityInput", "onBackPressed");
        super.onBackPressed();
    }

    @Override protected void onUserLeaveHint() {
        Diagnostics.info("MainActivityLifecycle", "onUserLeaveHint");
        super.onUserLeaveHint();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Diagnostics.info("LaunchPath", "onNewIntent; action=" + intent.getAction()
            + "; flags=" + Integer.toHexString(intent.getFlags()));
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Diagnostics.info("WindowStage", "focus=" + hasFocus + "; finishing=" + isFinishing());
    }

    @Override public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Diagnostics.info("WindowStage", "decor attached");
    }

    @Override public void onDetachedFromWindow() {
        Diagnostics.info("WindowStage", "decor detached");
        super.onDetachedFromWindow();
    }

    @Override public void onTrimMemory(int level) {
        Diagnostics.warn("MemoryStage", "onTrimMemory level=" + level);
        super.onTrimMemory(level);
    }

    @Override public void onLowMemory() {
        Diagnostics.warn("MemoryStage", "onLowMemory");
        super.onLowMemory();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        Diagnostics.info("MainActivityLifecycle", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override protected void onStart() {
        super.onStart();
        Diagnostics.info("MainActivityLifecycle", "onStart");
    }

    @Override protected void onResume() {
        super.onResume();
        Diagnostics.info("MainActivityLifecycle", "onResume; pid=" + Process.myPid());
        diagnosticHandler.removeCallbacks(heartbeat);
        diagnosticHandler.postDelayed(heartbeat, 2000);
    }

    @Override protected void onPause() {
        diagnosticHandler.removeCallbacks(heartbeat);
        Diagnostics.info("MainActivityLifecycle", "onPause; finishing=" + isFinishing());
        super.onPause();
    }

    @Override protected void onStop() {
        Diagnostics.info("MainActivityLifecycle", "onStop; finishing=" + isFinishing());
        super.onStop();
    }

    @Override protected void onDestroy() {
        diagnosticHandler.removeCallbacks(heartbeat);
        Diagnostics.info("MainActivityLifecycle", "onDestroy; finishing=" + isFinishing()
            + "; changingConfigurations=" + isChangingConfigurations());
        super.onDestroy();
    }
}
