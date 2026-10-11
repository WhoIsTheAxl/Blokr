package com.blokr.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import com.blokr.app.diagnostic.Diagnostics;

/** Initializes diagnostics before any Activity or engine startup. */
public final class BlokrApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Diagnostics.init(this);
        Diagnostics.installCrashHandler();
        Diagnostics.info("BlokrApplication", "Application created; pid=" + Process.myPid());
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            private void record(String event, Activity activity) {
                Diagnostics.info("GlobalLifecycle", event + "; activity="
                    + activity.getClass().getSimpleName() + "; task=" + activity.getTaskId()
                    + "; finishing=" + activity.isFinishing());
            }
            @Override public void onActivityCreated(Activity a, Bundle b) { record("created", a); }
            @Override public void onActivityStarted(Activity a) { record("started", a); }
            @Override public void onActivityResumed(Activity a) { record("resumed", a); }
            @Override public void onActivityPaused(Activity a) { record("paused", a); }
            @Override public void onActivityStopped(Activity a) { record("stopped", a); }
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) { record("saveState", a); }
            @Override public void onActivityDestroyed(Activity a) { record("destroyed", a); }
        });
        Thread watchdog = new Thread(() -> {
            long start = SystemClock.elapsedRealtime();
            while (true) {
                try {
                    Thread.sleep(1000);
                    Diagnostics.info("ProcessWatchdog", "alive; pid=" + Process.myPid()
                        + "; uptimeMs=" + (SystemClock.elapsedRealtime() - start));
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Throwable failure) {
                    android.util.Log.e("ProcessWatchdog", "Watchdog failure", failure);
                }
            }
        }, "Blokr-Process-Watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
        Diagnostics.info("BlokrApplication", "Independent watchdog started");
    }

    @Override public void onTrimMemory(int level) {
        Diagnostics.warn("AppMemory", "onTrimMemory level=" + level);
        super.onTrimMemory(level);
    }

    @Override public void onLowMemory() {
        Diagnostics.warn("AppMemory", "onLowMemory");
        super.onLowMemory();
    }

    @Override public void onTerminate() {
        Diagnostics.warn("BlokrApplication", "onTerminate (not guaranteed on devices)");
        super.onTerminate();
    }
}
