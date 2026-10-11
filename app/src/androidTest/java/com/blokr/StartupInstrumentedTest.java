package com.blokr;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * End-to-end smoke test: actual Android launcher intent -> splash -> MainActivity.
 * This tests lifecycle/navigation on an emulator, not GPU rendering or device-specific firmware.
 */
@RunWith(AndroidJUnit4.class)
public class StartupInstrumentedTest {
    @Test
    public void launcherTransitionsToMainAndRemainsAlive() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Intent launch = instrumentation.getTargetContext().getPackageManager()
            .getLaunchIntentForPackage(instrumentation.getTargetContext().getPackageName());
        assertNotNull("Missing launcher intent", launch);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Instrumentation.ActivityMonitor mainMonitor =
            instrumentation.addMonitor(MainActivity.class.getName(), null, false);
        Activity initial = null;
        Activity main = null;
        try {
            initial = instrumentation.startActivitySync(launch);
            assertTrue("Expected StartActivity, got " + initial.getClass().getName(),
                initial instanceof StartActivity);
            main = instrumentation.waitForMonitorWithTimeout(mainMonitor, 12000);
            assertNotNull("MainActivity did not appear within 12 seconds", main);
            assertFalse("MainActivity finished immediately", main.isFinishing());
            SystemClock.sleep(8000); // Covers delayed protocol probes and original ~2s failure.
            assertFalse("MainActivity unexpectedly finished", main.isFinishing());
            assertFalse("MainActivity unexpectedly destroyed", main.isDestroyed());
        } finally {
            instrumentation.removeMonitor(mainMonitor);
            if (main != null) main.finish();
            if (initial != null) initial.finish();
        }
    }
}
