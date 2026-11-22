package dev.rono.permissions.velocity;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.rono.permissions.core.platform.Platform;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

/**
 * Velocity platform. A proxy has no main thread, so "sync" work runs on the
 * scheduler too.
 */
final class VelocityPlatform implements Platform<CommandSource> {
    /**
     * Permission that receives operator broadcasts; proxies have no operator flag.
     */
    static final String NOTIFY_PERMISSION = "pex.notify";

    private final ProxyServer proxy;
    private final PluginContainer container;
    private final Object plugin;
    private final PlatformLogger logger;
    private final PlatformConfiguration configuration;
    private final PlatformScheduler scheduler;

    VelocityPlatform(ProxyServer proxy, PluginContainer container, Object plugin, Logger logger, Path dataDirectory) {
        this.proxy = proxy;
        this.container = container;
        this.plugin = plugin;
        this.logger = new PlatformLogger() {
            @Override
            public void info(String message) {
                logger.info(message);
            }

            @Override
            public void warn(String message) {
                logger.warn(message);
            }

            @Override
            public void error(String message, Throwable error) {
                logger.error(message, error);
            }
        };
        this.configuration = new PlatformConfiguration() {
            @Override
            public Path dataDirectory() {
                return dataDirectory;
            }

            @Override
            public void saveResource(String resource, boolean replace) {
                Path target = dataDirectory.resolve(resource);

                if (Files.exists(target) && !replace) {
                    return;
                }

                try (InputStream input = VelocityPlatform.class.getClassLoader().getResourceAsStream(resource)) {
                    if (input == null) {
                        throw new IllegalArgumentException("Bundled resource not found: " + resource);
                    }

                    Files.createDirectories(target.getParent());
                    Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException error) {
                    throw new UncheckedIOException("Unable to save " + resource, error);
                }
            }
        };
        this.scheduler = new Scheduler();
    }

    @Override
    public PlatformLogger logger() {
        return logger;
    }

    @Override
    public PlatformScheduler scheduler() {
        return scheduler;
    }

    @Override
    public PlatformConfiguration configuration() {
        return configuration;
    }

    @Override
    public Class<CommandSource> senderType() {
        return CommandSource.class;
    }

    @Override
    public void sendMessage(CommandSource sender, String message) {
        // Core formats with section-sign colour codes.
        sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Override
    public void broadcastToOperators(String message) {
        for (Player player : proxy.getAllPlayers()) {
            if (player.hasPermission(NOTIFY_PERMISSION)) {
                sendMessage(player, message);
            }
        }
    }

    @Override
    public CommandManager<CommandSource> createCommandManager() {
        return new VelocityCommandManager<>(container, proxy, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
    }

    @Override
    public Set<String> integrations() {
        return new LinkedHashSet<>();
    }

    @Override
    public Set<UUID> onlineUserIds() {
        Set<UUID> ids = new LinkedHashSet<>();

        for (Player player : proxy.getAllPlayers()) {
            ids.add(player.getUniqueId());
        }

        return ids;
    }

    private final class Scheduler implements PlatformScheduler {
        private final AtomicInteger nextId = new AtomicInteger();
        private final Map<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();

        @Override
        public void execute(Runnable task) {
            proxy.getScheduler().buildTask(plugin, task).schedule();
        }

        @Override
        public void executeAsync(Runnable task) {
            execute(task);
        }

        @Override
        public void executeLater(Runnable task, Duration delay) {
            proxy.getScheduler().buildTask(plugin, task).delay(delay).schedule();
        }

        @Override
        public void executeLaterAsync(Runnable task, Duration delay) {
            executeLater(task, delay);
        }

        @Override
        public void executeRepeating(Runnable task, Duration interval) {
            scheduleRepeating(task, interval);
        }

        @Override
        public void executeRepeatingAsync(Runnable task, Duration interval) {
            scheduleRepeating(task, interval);
        }

        @Override
        public int scheduleRepeating(Runnable task, Duration interval) {
            int id = nextId.incrementAndGet();

            tasks.put(id, proxy.getScheduler().buildTask(plugin, task).repeat(interval).schedule());

            return id;
        }

        @Override
        public int scheduleRepeatingAsync(Runnable task, Duration interval) {
            return scheduleRepeating(task, interval);
        }

        @Override
        public boolean isMainThread() {
            return false;
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
            for (ScheduledTask task : proxy.getScheduler().tasksByPlugin(plugin)) {
                task.cancel();
            }

            tasks.clear();
        }
    }
}
