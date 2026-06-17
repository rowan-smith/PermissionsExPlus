package dev.rono.permissions.sponge.platform;

import dev.rono.permissions.api.runtime.PlatformScheduler;
import org.spongepowered.api.Server;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;

import java.util.concurrent.TimeUnit;

public final class SpongePlatformScheduler implements PlatformScheduler {
    private final PluginContainer plugin;
    private final Server server;

    public SpongePlatformScheduler(PluginContainer plugin, Server server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void runSync(Runnable task) {
        server.scheduler().submit(Task.builder().plugin(plugin).execute(ignored -> task.run()).build());
    }

    @Override
    public void runAsync(Runnable task) {
        server.scheduler().submit(Task.builder().plugin(plugin).execute(ignored -> task.run()).build());
    }

    @Override
    public void runLater(Runnable task, long delayTicks) {
        server.scheduler()
                .submit(Task.builder()
                        .plugin(plugin)
                        .execute(ignored -> task.run())
                        .delay(delayTicks * 50L, TimeUnit.MILLISECONDS)
                        .build());
    }
}
