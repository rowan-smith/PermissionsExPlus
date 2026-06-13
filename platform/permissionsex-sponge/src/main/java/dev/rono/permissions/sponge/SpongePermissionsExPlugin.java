package dev.rono.permissions.sponge;

import com.google.inject.Inject;
import dev.rono.permissions.api.runtime.NoOpPlatformEventBus;
import dev.rono.permissions.api.runtime.PlatformRuntime;
import dev.rono.permissions.bungee.BungeePermissionsExConfig;
import dev.rono.permissions.bungee.backends.file.BungeeFileBackend;
import dev.rono.permissions.bungee.backends.memory.BungeeMemoryBackend;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.sponge.platform.SpongePlatformAdapter;
import dev.rono.permissions.sponge.platform.SpongePlatformScheduler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.nio.file.Path;

@Plugin("permissionsex")
public final class SpongePermissionsExPlugin {
    private final PluginContainer container;
    private final Logger logger;
    private final Path configDir;
    private PermissionManager manager;
    private PlatformRuntime platformRuntime;

    @Inject
    public SpongePermissionsExPlugin(PluginContainer container, Logger logger, @ConfigDir(sharedRoot = false) Path configDir) {
        this.container = container;
        this.logger = logger;
        this.configDir = configDir;
    }

    @Listener
    public void onEngineStart(StartedEngineEvent<Server> event) throws PermissionBackendException {
        Server server = event.engine();
        var config = new BungeePermissionsExConfig(
                configDir.toFile(), java.util.logging.Logger.getLogger(logger.getName()));
        PermissionBackend.registerBackendAlias("memory", BungeeMemoryBackend.class);
        PermissionBackend.registerBackendAlias("file", BungeeFileBackend.class);
        var adapter = new SpongePlatformAdapter(server);
        var scheduler = new SpongePlatformScheduler(container, server);
        platformRuntime = PlatformRuntime.of(adapter, NoOpPlatformEventBus.INSTANCE, scheduler);
        manager = new DefaultPermissionManager(config, java.util.logging.Logger.getLogger(logger.getName()), platformRuntime);
        manager.initTimer();
        logger.info("PermissionsExPlus Sponge adapter enabled");
    }

    @Listener
    public void onEngineStop(StoppingEngineEvent<Server> event) {
        if (manager != null) {
            manager.end();
            manager = null;
        }
        platformRuntime = null;
        logger.info("PermissionsExPlus Sponge adapter disabled");
    }

    public PermissionManager manager() {
        return manager;
    }
}
