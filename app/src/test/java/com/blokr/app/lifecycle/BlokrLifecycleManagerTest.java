package com.blokr.app.lifecycle;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlokrLifecycleManagerTest {
    private static BlokrLifecycleManager.ManagedResource resource(
            String name, List<String> events, boolean failStart, boolean failStop) {
        return new BlokrLifecycleManager.ManagedResource() {
            @Override public String name() { return name; }
            @Override public void start() { events.add("start:" + name); if (failStart) throw new IllegalStateException("start"); }
            @Override public void pause() { events.add("pause:" + name); }
            @Override public void resume() { events.add("resume:" + name); }
            @Override public void stop() { events.add("stop:" + name); if (failStop) throw new IllegalStateException("stop"); }
        };
    }

    @Test public void gracefulShutdownIsReverseOrderedAndIdempotent() throws Exception {
        List<String> events = new ArrayList<>();
        BlokrLifecycleManager manager = new BlokrLifecycleManager();
        manager.register(resource("network", events, false, false));
        manager.register(resource("world", events, false, false));
        manager.start();
        manager.pause();
        manager.resume();
        assertTrue(manager.shutdown().successful());
        assertTrue(manager.shutdown().successful());
        assertEquals(BlokrLifecycleManager.State.STOPPED, manager.state());
        assertEquals(Arrays.asList("start:network", "start:world",
            "pause:world", "pause:network", "resume:network", "resume:world",
            "stop:world", "stop:network"), events);
    }

    @Test public void cleanupContinuesAfterFailure() throws Exception {
        List<String> events = new ArrayList<>();
        BlokrLifecycleManager manager = new BlokrLifecycleManager();
        manager.register(resource("first", events, false, false));
        manager.register(resource("second", events, false, true));
        manager.start();
        assertEquals(1, manager.shutdown().failures().size());
        assertEquals(Arrays.asList("start:first", "start:second", "stop:second", "stop:first"), events);
        assertEquals(BlokrLifecycleManager.State.FAILED, manager.state());
    }

    @Test public void failedStartupRollsBackPartiallyStartedResource() {
        List<String> events = new ArrayList<>();
        BlokrLifecycleManager manager = new BlokrLifecycleManager();
        manager.register(resource("first", events, false, false));
        manager.register(resource("second", events, true, false));
        try {
            manager.start();
            fail("Expected startup failure");
        } catch (Exception expected) {
            assertEquals(Arrays.asList("start:first", "start:second", "stop:second", "stop:first"), events);
            assertEquals(BlokrLifecycleManager.State.STOPPED, manager.state());
        }
    }

    @Test public void cannotRestartAfterShutdown() throws Exception {
        BlokrLifecycleManager manager = new BlokrLifecycleManager();
        manager.shutdown();
        try {
            manager.start();
            fail("Expected invalid transition");
        } catch (IllegalStateException expected) {
            assertEquals(BlokrLifecycleManager.State.STOPPED, manager.state());
        }
    }
}
