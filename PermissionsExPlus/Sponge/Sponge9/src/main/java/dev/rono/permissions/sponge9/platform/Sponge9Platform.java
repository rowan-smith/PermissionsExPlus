package dev.rono.permissions.sponge9.platform;

import cloud.commandframework.CommandManager;
import dev.rono.permissions.core.platform.Platform;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import dev.rono.permissions.sponge9.command.Sponge9CommandManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.plugin.PluginContainer;

public final class Sponge9Platform implements Platform<CommandCause> {
    /**
     * Permission that receives operator broadcasts; Sponge has no operator flag.
     */
    public static final String NOTIFY_PERMISSION = "pex.notify";

    private final PlatformLogger logger;
    private final Sponge9Scheduler scheduler;
    private final PlatformConfiguration configuration;
    private final Sponge9CommandManager commands;

    public Sponge9Platform(PluginContainer plugin, Logger logger, Path dataDirectory) {
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
        this.scheduler = new Sponge9Scheduler(plugin);
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

                try (InputStream input = Sponge9Platform.class.getClassLoader().getResourceAsStream(resource)) {
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
        this.commands = new Sponge9CommandManager(plugin, this.logger);
    }

    /**
     * The command manager; its roots are registered during RegisterCommandEvent.
     */
    public Sponge9CommandManager commands() {
        return commands;
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
    public Class<CommandCause> senderType() {
        return CommandCause.class;
    }

    @Override
    public void sendMessage(CommandCause sender, String message) {
        // Core formats with section-sign colour codes.
        sender.audience().sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Override
    public void broadcastToOperators(String message) {
        for (ServerPlayer player : Sponge.server().onlinePlayers()) {
            if (player.hasPermission(NOTIFY_PERMISSION)) {
                player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
            }
        }
    }

    @Override
    public CommandManager<CommandCause> createCommandManager() {
        return commands;
    }

    @Override
    public Set<UUID> onlineUserIds() {
        Set<UUID> ids = new LinkedHashSet<>();

        if (Sponge.isServerAvailable()) {
            for (ServerPlayer player : Sponge.server().onlinePlayers()) {
                ids.add(player.uniqueId());
            }
        }

        return ids;
    }
}
