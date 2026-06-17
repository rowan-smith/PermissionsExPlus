package dev.rono.permissions.api.runtime;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * JVM-backed scheduler used by tests and as a fallback when no host scheduler is wired.
 */
public final class DirectPlatformScheduler implements PlatformScheduler {

    public static final DirectPlatformScheduler INSTANCE = new DirectPlatformScheduler();

    private volatile ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private DirectPlatformScheduler() {}

    @Override
    public void runSync(Runnable task) {
        task.run();
    }

    @Override
    public void runAsync(Runnable task) {
        executor().execute(task);
    }

    @Override
    public void runLater(Runnable task, long delayTicks) {
        executor().schedule(task, delayTicks * 50L, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedules {@code task} after {@code delaySeconds} using wall-clock timing.
     */
    public void runLaterSeconds(Runnable task, long delaySeconds) {
        executor().schedule(task, delaySeconds, TimeUnit.SECONDS);
    }

    ScheduledExecutorService executor() {
        ScheduledExecutorService current = executor;
        if (current == null || current.isShutdown()) {
            synchronized (this) {
                current = executor;
                if (current == null || current.isShutdown()) {
                    executor = Executors.newSingleThreadScheduledExecutor();
                    current = executor;
                }
            }
        }
        return current;
    }

    public void shutdown() {
        ScheduledExecutorService current = executor;
        if (current != null) {
            current.shutdown();
        }
        executor = null;
    }
}
