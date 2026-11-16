package dev.rono.permissions.sponge7.platform;

import cloud.commandframework.CommandManager;
import dev.rono.permissions.core.platform.Platform;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import dev.rono.permissions.sponge7.command.Sponge7CommandManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.serializer.TextSerializers;

public final class Sponge7Platform implements Platform<CommandSource> {
    /**
     * Permission that receives operator broadcasts; Sponge has no operator flag.
     */
    public static final String NOTIFY_PERMISSION = "pex.notify";

    private final Object plugin;
    private final PlatformLogger logger;
    private final Sponge7Scheduler scheduler;
    private final PlatformConfiguration configuration;

    public Sponge7Platform(Object plugin, Logger logger, Path dataDirectory) {
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
        this.scheduler = new Sponge7Scheduler(plugin);
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

                try (InputStream input = Sponge7Platform.class.getClassLoader().getResourceAsStream(resource)) {
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
        sender.sendMessage(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message));
    }

    @Override
    public void broadcastToOperators(String message) {
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (player.hasPermission(NOTIFY_PERMISSION)) {
                sendMessage(player, message);
            }
        }
    }

    @Override
    public CommandManager<CommandSource> createCommandManager() {
        return new Sponge7CommandManager(plugin, logger);
    }

    @Override
    public Set<UUID> onlineUserIds() {
        Set<UUID> ids = new LinkedHashSet<>();

        if (Sponge.isServerAvailable()) {
            for (Player player : Sponge.getServer().getOnlinePlayers()) {
                ids.add(player.getUniqueId());
            }
        }

        return ids;
    }
}
