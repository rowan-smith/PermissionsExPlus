package dev.rono.permissions.sponge7.platform;

import dev.rono.permissions.core.platform.PlatformScheduler;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

/**
 * Sponge task scheduler. Sponge identifies tasks by UUID; Core cancels by int,
 * so
 * repeating tasks are tracked under locally assigned ids.
 */
public final class Sponge7Scheduler implements PlatformScheduler {
    private final Object plugin;
    private final AtomicInteger nextId = new AtomicInteger();
    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();

    public Sponge7Scheduler(Object plugin) {
        this.plugin = plugin;
    }

    private Task.Builder builder(Runnable task) {
        return Sponge.getScheduler().createTaskBuilder().execute(task);
    }

    @Override
    public void execute(Runnable task) {
        builder(task).submit(plugin);
    }

    @Override
    public void executeAsync(Runnable task) {
        builder(task).async().submit(plugin);
    }

    @Override
    public void executeLater(Runnable task, Duration delay) {
        builder(task).delay(delay.toMillis(), TimeUnit.MILLISECONDS).submit(plugin);
    }

    @Override
    public void executeLaterAsync(Runnable task, Duration delay) {
        builder(task).async().delay(delay.toMillis(), TimeUnit.MILLISECONDS).submit(plugin);
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
        return track(builder(task).interval(interval.toMillis(), TimeUnit.MILLISECONDS).submit(plugin));
    }

    @Override
    public int scheduleRepeatingAsync(Runnable task, Duration interval) {
        return track(builder(task).async().interval(interval.toMillis(), TimeUnit.MILLISECONDS).submit(plugin));
    }

    private int track(Task task) {
        int id = nextId.incrementAndGet();

        tasks.put(id, task);

        return id;
    }

    @Override
    public boolean isMainThread() {
        return Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
    }

    @Override
    public void cancelTask(int taskId) {
        Task task = tasks.remove(taskId);

        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void cancelTasks() {
        for (Task task : Sponge.getScheduler().getScheduledTasks(plugin)) {
            task.cancel();
        }

        tasks.clear();
    }
}
