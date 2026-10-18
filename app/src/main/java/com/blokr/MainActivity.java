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
import com.blokr.app.ui.UIFactory;
import com.blokr.app.ui.UIManager;
import com.blokr.app.lifecycle.BlokrLifecycleManager;

/** Android launcher for the bridge and graphics bootstrap. */
public final class MainActivity extends Activity {
    private UIManager uiManager;
    private BlokrLifecycleManager lifecycleManager;
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
        UIFactory uiFactory = new UIFactory(this);
        uiManager = new UIManager(this);
        lifecycleManager = new BlokrLifecycleManager();
        lifecycleManager.register(new BlokrLifecycleManager.ManagedResource() {
            @Override public String name() { return "LauncherUI"; }
            @Override public void start() {}
            @Override public void stop() { uiManager.dispose(); }
        });
        try {
            lifecycleManager.start();
        } catch (Exception failure) {
            Diagnostics.error("MainActivity", "Launcher infrastructure failed", failure);
            throw new IllegalStateException("Launcher infrastructure failed", failure);
        }
        com.blokr.app.ui.MinecraftLauncherView launcher = new com.blokr.app.ui.MinecraftLauncherView(this);
        TextView status = launcher.diagnosticsView();
        View launcherView = launcher.create();
        Diagnostics.info("StartupStage", "03 Minecraft-inspired launcher created");
        uiManager.register("launcher", factory -> launcherView);
        if (savedInstanceState != null && "launcher".equals(savedInstanceState.getString("blokr.ui.current"))) {
            uiManager.restoreState(savedInstanceState);
        } else {
            uiManager.show("launcher");
        }
        Diagnostics.info("StartupStage", "05 after setContentView");
        launcherView.post(() -> Diagnostics.info("RenderStage", "launcher shown=" + launcherView.isShown()));
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
                Diagnostics.info("WorldState", "Checking sparse world storage");
                String result = MinosoftCoreProbe.checkWorldState();
                Diagnostics.info("WorldState", result);
                report.append("\n").append(result);
            } catch (Throwable failure) {
                Diagnostics.error("WorldState", "Portable world-state check failed", failure);
                report.append("\nPortable world state: FAIL");
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
        if (uiManager != null) uiManager.saveState(outState);
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
        diagnosticHandler.removeCallbacksAndMessages(null);
        if (lifecycleManager != null) {
            BlokrLifecycleManager.ShutdownReport report = lifecycleManager.shutdown();
            if (!report.successful()) Diagnostics.warn("MainActivityLifecycle", "UI cleanup failures=" + report.failures().size());
        }
        Diagnostics.info("MainActivityLifecycle", "onDestroy; finishing=" + isFinishing()
            + "; changingConfigurations=" + isChangingConfigurations());
        super.onDestroy();
    }
}
