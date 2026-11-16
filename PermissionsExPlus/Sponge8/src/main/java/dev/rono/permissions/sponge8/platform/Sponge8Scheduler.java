package dev.rono.permissions.sponge8.platform;

import dev.rono.permissions.core.platform.PlatformScheduler;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;

/**
 * Sponge task scheduler. Sponge identifies tasks by UUID; Core cancels by int,
 * so
 * repeating tasks are tracked under locally assigned ids. Before the server
 * exists
 * (early lifecycle) "sync" work runs on the async scheduler.
 */
public final class Sponge8Scheduler implements PlatformScheduler {
    private final PluginContainer plugin;
    private final AtomicInteger nextId = new AtomicInteger();
    private final Map<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();

    public Sponge8Scheduler(PluginContainer plugin) {
        this.plugin = plugin;
    }

    private static Scheduler sync() {
        return Sponge.isServerAvailable() ? Sponge.server().scheduler() : Sponge.asyncScheduler();
    }

    private Task task(Runnable runnable, Duration delay, Duration interval) {
        Task.Builder builder = Task.builder().plugin(plugin).execute(scheduled -> runnable.run());

        if (delay != null) {
            builder.delay(delay);
        }

        if (interval != null) {
            builder.interval(interval);
        }

        return builder.build();
    }

    @Override
    public void execute(Runnable task) {
        sync().submit(task(task, null, null));
    }

    @Override
    public void executeAsync(Runnable task) {
        Sponge.asyncScheduler().submit(task(task, null, null));
    }

    @Override
    public void executeLater(Runnable task, Duration delay) {
        sync().submit(task(task, delay, null));
    }

    @Override
    public void executeLaterAsync(Runnable task, Duration delay) {
        Sponge.asyncScheduler().submit(task(task, delay, null));
    }

    @Override
    public void executeRepeating(Runnable task, Duration interval) {
        scheduleRepeating(task, interval);
    }

    @Override
    public void executeRepeatingAsync(Runnable task, Duration interval) {
        scheduleRepeatingAsync(task, interval);
    }

    @Override
    public int scheduleRepeating(Runnable task, Duration interval) {
        return track(sync().submit(task(task, null, interval)));
    }

    @Override
    public int scheduleRepeatingAsync(Runnable task, Duration interval) {
        return track(Sponge.asyncScheduler().submit(task(task, null, interval)));
    }

    private int track(ScheduledTask task) {
        int id = nextId.incrementAndGet();

        tasks.put(id, task);

        return id;
    }

    @Override
    public boolean isMainThread() {
        return Sponge.isServerAvailable() && Sponge.server().onMainThread();
    }

    @Override
    public void cancelTask(int taskId) {
        ScheduledTask task = tasks.remove(taskId);

        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void cancelTasks() {
        for (ScheduledTask task : Sponge.asyncScheduler().tasks(plugin)) {
            task.cancel();
        }

        if (Sponge.isServerAvailable()) {
            for (ScheduledTask task : Sponge.server().scheduler().tasks(plugin)) {
                task.cancel();
            }
        }

        tasks.clear();
    }
}
