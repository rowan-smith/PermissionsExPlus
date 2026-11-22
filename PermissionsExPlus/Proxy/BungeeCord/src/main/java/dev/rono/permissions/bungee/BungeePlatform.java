package dev.rono.permissions.bungee;

import cloud.commandframework.CommandManager;
import cloud.commandframework.bungee.BungeeCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * BungeeCord platform. A proxy has no main thread, so all work runs on the
 * scheduler.
 */
final class BungeePlatform implements Platform<CommandSender> {
    /**
     * Permission that receives operator broadcasts; proxies have no operator flag.
     */
    static final String NOTIFY_PERMISSION = "pex.notify";

    private final Plugin plugin;
    private final PlatformLogger logger;
    private final PlatformConfiguration configuration;
    private final PlatformScheduler scheduler;

    BungeePlatform(Plugin plugin) {
        this.plugin = plugin;
        this.logger = new PlatformLogger() {
            @Override
            public void info(String message) {
                plugin.getLogger().info(message);
            }

            @Override
            public void warn(String message) {
                plugin.getLogger().warning(message);
            }

            @Override
            public void error(String message, Throwable error) {
                plugin.getLogger().log(Level.SEVERE, message, error);
            }
        };
        this.configuration = new PlatformConfiguration() {
            @Override
            public Path dataDirectory() {
                return plugin.getDataFolder().toPath();
            }

            @Override
            public void saveResource(String resource, boolean replace) {
                Path target = dataDirectory().resolve(resource);

                if (Files.exists(target) && !replace) {
                    return;
                }

                try (InputStream input = plugin.getResourceAsStream(resource)) {
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
        this.scheduler = new PlatformScheduler() {
            @Override
            public void execute(Runnable task) {
                plugin.getProxy().getScheduler().runAsync(plugin, task);
            }

            @Override
            public void executeAsync(Runnable task) {
                execute(task);
            }

            @Override
            public void executeLater(Runnable task, Duration delay) {
                plugin.getProxy().getScheduler().schedule(plugin, task, delay.toMillis(), TimeUnit.MILLISECONDS);
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
                return plugin.getProxy().getScheduler().schedule(plugin, task, 0, interval.toMillis(), TimeUnit.MILLISECONDS).getId();
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
                plugin.getProxy().getScheduler().cancel(taskId);
            }

            @Override
            public void cancelTasks() {
                plugin.getProxy().getScheduler().cancel(plugin);
            }
        };
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
    public Class<CommandSender> senderType() {
        return CommandSender.class;
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        // Core formats with section-sign colour codes.
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void broadcastToOperators(String message) {
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            if (player.hasPermission(NOTIFY_PERMISSION)) {
                sendMessage(player, message);
            }
        }
    }

    @Override
    public CommandManager<CommandSender> createCommandManager() throws Exception {
        return new BungeeCommandManager<>(plugin, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
    }

    @Override
    public Set<UUID> onlineUserIds() {
        Set<UUID> ids = new LinkedHashSet<>();

        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            ids.add(player.getUniqueId());
        }

        return ids;
    }
}
