package com.blokr.app.platform;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import de.bixilon.minosoft.platform.EnginePlatform;
import de.bixilon.minosoft.platform.LogLevel;
import java.io.File;

/** Android services consumed by portable Minosoft engine code. */
public final class AndroidEnginePlatform implements EnginePlatform {
    private final Context application;

    public AndroidEnginePlatform(Context context) {
        if (context == null) throw new IllegalArgumentException("context == null");
        application = context.getApplicationContext();
    }

    @Override public File dataDirectory() {
        return new File(application.getFilesDir(), "minosoft");
    }

    @Override public long monotonicTimeNanos() {
        return SystemClock.elapsedRealtimeNanos();
    }

    @Override public void log(LogLevel level, String tag, String message, Throwable failure) {
        String label = "Minosoft/" + tag;
        switch (level) {
            case DEBUG: Log.d(label, message, failure); break;
            case INFO: Log.i(label, message, failure); break;
            case WARN: Log.w(label, message, failure); break;
            case ERROR: Log.e(label, message, failure); break;
            default: throw new IllegalArgumentException("Unknown log level: " + level);
        }
    }
}
