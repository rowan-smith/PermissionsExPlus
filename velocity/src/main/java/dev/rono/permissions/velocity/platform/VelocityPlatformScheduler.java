package dev.rono.permissions.velocity.platform;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.rono.permissions.api.runtime.PlatformScheduler;

import java.util.concurrent.TimeUnit;

public final class VelocityPlatformScheduler implements PlatformScheduler {
    private final Object plugin;
    private final ProxyServer server;

    public VelocityPlatformScheduler(Object plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void runSync(Runnable task) {
        server.getScheduler().buildTask(plugin, task).schedule();
    }

    @Override
    public void runAsync(Runnable task) {
        server.getScheduler().buildTask(plugin, task).schedule();
    }

    @Override
    public void runLater(Runnable task, long delayTicks) {
        server.getScheduler()
                .buildTask(plugin, task)
                .delay(delayTicks * 50L, TimeUnit.MILLISECONDS)
                .schedule();
    }
}
