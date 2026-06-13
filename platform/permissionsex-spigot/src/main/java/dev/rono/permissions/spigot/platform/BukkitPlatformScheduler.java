package dev.rono.permissions.spigot.platform;

import dev.rono.permissions.api.runtime.PlatformScheduler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Bukkit scheduler bridge for timed tasks and main-thread work.
 */
public final class BukkitPlatformScheduler implements PlatformScheduler {
    private final JavaPlugin plugin;

    public BukkitPlatformScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runSync(Runnable task) {
        scheduler().runTask(plugin, task);
    }

    @Override
    public void runAsync(Runnable task) {
        scheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runLater(Runnable task, long delayTicks) {
        scheduler().runTaskLater(plugin, task, delayTicks);
    }

    /**
     * Schedules {@code task} after {@code delaySeconds} using Bukkit tick conversion (20 ticks per second).
     */
    public void runLaterSeconds(Runnable task, long delaySeconds) {
        runLater(task, delaySeconds * 20L);
    }

    private BukkitScheduler scheduler() {
        return plugin.getServer().getScheduler();
    }
}
