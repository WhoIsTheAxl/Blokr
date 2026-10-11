package com.blokr.app.ui;

import android.app.Activity;
import android.os.Looper;
import android.view.View;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity-scoped screen registry and navigation controller.
 * Does not retain a static Activity reference. Host owns and disposes the manager.
 */
public final class UIManager {
    public interface Screen {
        View create(UIFactory factory);
    }

    private final Activity activity;
    private final UIFactory factory;
    private final Map<String, Screen> screens = new HashMap<>();
    private final ArrayDeque<String> history = new ArrayDeque<>();
    private String current;
    private boolean disposed;

    public UIManager(Activity activity) {
        if (activity == null) throw new IllegalArgumentException("activity == null");
        this.activity = activity;
        factory = new UIFactory(activity);
    }

    private void assertMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper())
            throw new IllegalStateException("UI navigation must run on main thread");
        if (disposed) throw new IllegalStateException("UIManager disposed");
    }

    public void register(String key, Screen screen) {
        assertMainThread();
        if (key == null || key.trim().isEmpty() || screen == null)
            throw new IllegalArgumentException("Invalid screen registration");
        if (screens.containsKey(key)) throw new IllegalArgumentException("Duplicate screen: " + key);
        screens.put(key, screen);
    }

    public void show(String key) {
        assertMainThread();
        Screen screen = screens.get(key);
        if (screen == null) throw new IllegalArgumentException("Unknown screen: " + key);
        if (key.equals(current)) return;
        // Construct before modifying navigation history, so creation failures preserve state.
        View view = screen.create(factory);
        if (view == null) throw new IllegalStateException("Screen returned null: " + key);
        activity.setContentView(view);
        if (current != null) history.push(current);
        current = key;
    }

    public boolean back() {
        assertMainThread();
        if (history.isEmpty()) return false;
        String previous = history.peek();
        Screen screen = screens.get(previous);
        if (screen == null) throw new IllegalStateException("Missing previous screen: " + previous);
        View view = screen.create(factory);
        if (view == null) throw new IllegalStateException("Screen returned null: " + previous);
        activity.setContentView(view);
        history.pop();
        current = previous;
        return true;
    }

    public String currentScreen() { return current; }

    public void dispose() {
        assertMainThread();
        disposed = true;
        screens.clear();
        history.clear();
        current = null;
    }
}
