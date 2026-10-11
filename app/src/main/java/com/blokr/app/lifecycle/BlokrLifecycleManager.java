package com.blokr.app.lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Coordinates application-owned services without depending on Android Activity lifetimes.
 * Register resources in acquisition order; shutdown releases them in reverse order.
 * Shutdown is idempotent, attempts every cleanup, and reports aggregated failures.
 *
 * This is an opt-in manager: existing launcher startup is intentionally unchanged.
 */
public final class BlokrLifecycleManager {
    public enum State { NEW, STARTING, RUNNING, PAUSED, STOPPING, STOPPED, FAILED }

    public interface ManagedResource {
        String name();
        void start() throws Exception;
        void stop() throws Exception;
        default void pause() throws Exception {}
        default void resume() throws Exception {}
    }

    public static final class ShutdownReport {
        private final List<Throwable> failures;
        private ShutdownReport(List<Throwable> failures) {
            this.failures = Collections.unmodifiableList(new ArrayList<>(failures));
        }
        public boolean successful() { return failures.isEmpty(); }
        public List<Throwable> failures() { return failures; }
    }

    private final List<ManagedResource> resources = new ArrayList<>();
    private final List<ManagedResource> started = new ArrayList<>();
    private State state = State.NEW;
    private ShutdownReport lastShutdown;

    public synchronized State state() { return state; }

    /** Registration is only allowed before the first startup. */
    public synchronized void register(ManagedResource resource) {
        if (resource == null) throw new IllegalArgumentException("resource == null");
        if (state != State.NEW) throw new IllegalStateException("Registration closed: " + state);
        if (resources.contains(resource)) throw new IllegalArgumentException("Duplicate resource");
        resources.add(resource);
    }

    public synchronized void start() throws Exception {
        if (state != State.NEW) throw new IllegalStateException("Cannot start from " + state);
        state = State.STARTING;
        try {
            for (ManagedResource resource : resources) {
                // Include the currently starting resource in rollback in case it partially starts.
                started.add(resource);
                resource.start();
            }
            state = State.RUNNING;
        } catch (Exception failure) {
            state = State.FAILED;
            ShutdownReport report = shutdown();
            for (Throwable cleanup : report.failures()) failure.addSuppressed(cleanup);
            throw failure;
        }
    }

    public synchronized void pause() throws Exception {
        if (state != State.RUNNING) throw new IllegalStateException("Cannot pause from " + state);
        try {
            for (int i = started.size() - 1; i >= 0; i--) started.get(i).pause();
            state = State.PAUSED;
        } catch (Exception failure) {
            state = State.FAILED;
            throw failure;
        }
    }

    public synchronized void resume() throws Exception {
        if (state != State.PAUSED) throw new IllegalStateException("Cannot resume from " + state);
        try {
            for (ManagedResource resource : started) resource.resume();
            state = State.RUNNING;
        } catch (Exception failure) {
            state = State.FAILED;
            throw failure;
        }
    }

    /**
     * Releases every started resource in reverse order, even if an earlier cleanup fails.
     * Repeated calls return the same report without invoking cleanup again.
     */
    public synchronized ShutdownReport shutdown() {
        if (lastShutdown != null) return lastShutdown;
        state = State.STOPPING;
        List<Throwable> failures = new ArrayList<>();
        for (int i = started.size() - 1; i >= 0; i--) {
            ManagedResource resource = started.get(i);
            try {
                resource.stop();
            } catch (Throwable failure) {
                failures.add(new IllegalStateException("Shutdown failed: " + resource.name(), failure));
            }
        }
        started.clear();
        lastShutdown = new ShutdownReport(failures);
        state = failures.isEmpty() ? State.STOPPED : State.FAILED;
        return lastShutdown;
    }
}
