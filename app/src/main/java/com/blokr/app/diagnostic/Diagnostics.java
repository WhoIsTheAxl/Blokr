package com.blokr.app.diagnostic;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * App-owned diagnostic log files. Avoid logging credentials, tokens,
 * session payloads, or user-generated private content.
 */
public final class Diagnostics {
    private static final String TAG = "Blokr";
    private static final long MAX_BYTES = 2L * 1024 * 1024;
    private static File directory;
    private static File current;
    private static volatile boolean initialized;
    private static volatile boolean crashHandlerInstalled;

    private Diagnostics() {}

    public static synchronized void init(Context context) {
        if (initialized) return;
        File external = context.getExternalFilesDir(null);
        // App-specific root: Android/data/<applicationId>/diagnostic/logs
        File root = external != null ? external.getParentFile() : null;
        File requested = root == null ? null : new File(root, "diagnostic/logs");
        if (requested != null && (requested.isDirectory() || requested.mkdirs())) {
            directory = requested;
        } else {
            directory = new File(context.getFilesDir(), "diagnostic/logs");
            if (!directory.isDirectory() && !directory.mkdirs()) {
                Log.e(TAG, "Unable to create diagnostic log directory");
                return;
            }
        }
        current = new File(directory, "blokr.log");
        rotateIfNeeded();
        initialized = true;
        info("Diagnostics", "Started; package=" + context.getPackageName()
            + "; android=" + Build.VERSION.SDK_INT
            + "; device=" + Build.MANUFACTURER + "/" + Build.MODEL);
        info("Diagnostics", "Log directory: " + directory.getAbsolutePath());
    }

    public static synchronized String getLogDirectory() {
        return directory == null ? "not initialized" : directory.getAbsolutePath();
    }

    public static void info(String tag, String message) {
        Log.i(tag, message);
        append("INFO", tag, message, null);
    }

    public static void warn(String tag, String message) {
        Log.w(tag, message);
        append("WARN", tag, message, null);
    }

    public static void error(String tag, String message, Throwable cause) {
        Log.e(tag, message, cause);
        append("ERROR", tag, message, cause);
    }

    private static synchronized void append(String level, String tag, String message, Throwable cause) {
        if (!initialized || current == null) return;
        rotateIfNeeded();
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(current, true)))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
            writer.printf("%s [%s] [%s] [thread=%s] %s%n",
                timestamp, level, tag, Thread.currentThread().getName(), message);
            if (cause != null) cause.printStackTrace(writer);
            writer.flush();
            if (writer.checkError()) Log.e(TAG, "Diagnostic log write failed");
        } catch (IOException e) {
            Log.e(TAG, "Failed to write diagnostic log", e);
        }
    }

    private static void rotateIfNeeded() {
        if (current == null || !current.exists() || current.length() < MAX_BYTES) return;
        File previous = new File(directory, "blokr.previous.log");
        if (previous.exists() && !previous.delete()) Log.w(TAG, "Unable to delete old log");
        if (!current.renameTo(previous)) Log.w(TAG, "Unable to rotate diagnostic log");
    }

    public static synchronized void installCrashHandler() {
        if (crashHandlerInstalled) return;
        crashHandlerInstalled = true;
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            try {
                error("UncaughtException", "Fatal error on thread " + thread.getName(), error);
            } catch (Throwable loggingFailure) {
                Log.e(TAG, "Crash diagnostics failed", loggingFailure);
            } finally {
                if (previous != null) previous.uncaughtException(thread, error);
            }
        });
    }
}
