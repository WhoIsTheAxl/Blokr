package com.blokr.app;

import android.app.Application;
import com.blokr.app.diagnostic.Diagnostics;

/** Initializes diagnostics before any Activity or engine startup. */
public final class BlokrApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Diagnostics.init(this);
        Diagnostics.installCrashHandler();
        Diagnostics.info("BlokrApplication", "Application created");
    }
}
