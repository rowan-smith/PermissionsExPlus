package dev.rono.permissions.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.rono.permissions.api.runtime.NoOpPlatformEventBus;
import dev.rono.permissions.api.runtime.PlatformRuntime;
import dev.rono.permissions.bungee.BungeePermissionsExConfig;
import dev.rono.permissions.bungee.backends.file.BungeeFileBackend;
import dev.rono.permissions.bungee.backends.memory.BungeeMemoryBackend;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.velocity.platform.VelocityPlatformAdapter;
import dev.rono.permissions.velocity.platform.VelocityPlatformScheduler;
import org.slf4j.Logger;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.nio.file.Path;

@Plugin(
        id = "permissionsex",
        name = "PermissionsExPlus",
        version = "1.23.5",
        authors = {"PermissionsExPlus"})
public final class VelocityPermissionsExPlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private PermissionManager manager;
    private PlatformRuntime platformRuntime;

    @Inject
    public VelocityPermissionsExPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) throws PermissionBackendException {
        BungeePermissionsExConfig config =
                new BungeePermissionsExConfig(dataDirectory.toFile(), java.util.logging.Logger.getLogger(logger.getName()));
        PermissionBackend.registerBackendAlias("memory", BungeeMemoryBackend.class);
        PermissionBackend.registerBackendAlias("file", BungeeFileBackend.class);
        var adapter = new VelocityPlatformAdapter(server);
        var scheduler = new VelocityPlatformScheduler(this, server);
        platformRuntime = PlatformRuntime.of(adapter, NoOpPlatformEventBus.INSTANCE, scheduler);
        manager = new DefaultPermissionManager(config, java.util.logging.Logger.getLogger("PermissionsEx"), platformRuntime);
        manager.initTimer();
        logger.info("PermissionsExPlus Velocity adapter enabled");
    }

    @Subscribe
    public void onPermissionSetup(PermissionsSetupEvent event) {
        event.setProvider(subject -> permission -> evaluate(subject, permission));
    }

    private Tristate evaluate(com.velocitypowered.api.permission.PermissionSubject subject, String permission) {
        if (manager == null || !(subject instanceof Player player)) {
            return Tristate.UNDEFINED;
        }
        try {
            PermissionUser user = manager.getUser(player.getUniqueId());
            String realm = player.getCurrentServer()
                    .map(c -> c.getServerInfo().getName())
                    .orElse(null);
            boolean allowed = realm == null ? user.has(permission) : user.has(permission, realm);
            return allowed ? Tristate.TRUE : Tristate.FALSE;
        } catch (RuntimeException ex) {
            return Tristate.UNDEFINED;
        }
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        if (manager != null) {
            manager.end();
            manager = null;
        }
        platformRuntime = null;
        logger.info("PermissionsExPlus Velocity adapter disabled");
    }
}
