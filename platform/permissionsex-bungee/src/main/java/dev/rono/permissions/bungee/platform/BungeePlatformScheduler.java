package dev.rono.permissions.bungee.platform;

import dev.rono.permissions.api.runtime.PlatformScheduler;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * BungeeCord scheduler bridge.
 */
public final class BungeePlatformScheduler implements PlatformScheduler {
    private final Plugin plugin;

    public BungeePlatformScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runSync(Runnable task) {
        plugin.getProxy().getScheduler().schedule(plugin, task, 0L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runAsync(Runnable task) {
        plugin.getProxy().getScheduler().runAsync(plugin, task);
    }

    @Override
    public void runLater(Runnable task, long delayTicks) {
        plugin.getProxy().getScheduler().schedule(plugin, task, delayTicks * 50L, TimeUnit.MILLISECONDS);
    }

    public void runLaterSeconds(Runnable task, long delaySeconds) {
        plugin.getProxy().getScheduler().schedule(plugin, task, delaySeconds, TimeUnit.SECONDS);
    }
}
