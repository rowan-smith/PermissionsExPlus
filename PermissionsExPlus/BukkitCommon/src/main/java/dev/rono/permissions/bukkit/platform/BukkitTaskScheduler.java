package dev.rono.permissions.bukkit.platform;

import dev.rono.permissions.core.platform.PlatformScheduler;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public class BukkitTaskScheduler implements PlatformScheduler {

    private final JavaPlugin plugin;

    public BukkitTaskScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public void executeAsync(Runnable task) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void executeLater(Runnable task, Duration delay) {
        plugin.getServer().getScheduler().runTaskLater(plugin, task, delay.toMillis() / 50);
    }

    @Override
    public void executeLaterAsync(Runnable task, Duration delay) {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, task, delay.toMillis() / 50);
    }

    @Override
    public void executeRepeating(Runnable task, Duration interval) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, task, 0, interval.toMillis() / 50);
    }

    @Override
    public void executeRepeatingAsync(Runnable task, Duration interval) {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, task, 0, interval.toMillis() / 50);
    }

    @Override
    public int scheduleRepeating(Runnable task, Duration interval) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, task, 0, interval.toMillis() / 50).getTaskId();
    }

    @Override
    public int scheduleRepeatingAsync(Runnable task, Duration interval) {
        return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, task, 0, interval.toMillis() / 50).getTaskId();
    }

    @Override
    public boolean isMainThread() {
        return plugin.getServer().isPrimaryThread();
    }

    @Override
    public void cancelTask(int taskId) {
        plugin.getServer().getScheduler().cancelTask(taskId);
    }

    @Override
    public void cancelTasks() {
        plugin.getServer().getScheduler().cancelTasks(plugin);
    }
}
