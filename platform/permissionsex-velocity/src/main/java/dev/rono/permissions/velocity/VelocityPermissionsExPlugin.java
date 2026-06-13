package dev.rono.permissions.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.rono.permissions.api.runtime.NoOpPlatformEventBus;
import dev.rono.permissions.api.runtime.PlatformRuntime;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.runtime.startup.ProxyPlatformInitializer;
import dev.rono.permissions.runtime.startup.VelocityPermissionBootstrapReporter;
import dev.rono.permissions.velocity.platform.VelocityPlatformAdapter;
import dev.rono.permissions.velocity.platform.VelocityPlatformScheduler;
import org.slf4j.Logger;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.nio.file.Path;

/**
 * Velocity proxy entry point. Shared proxy bootstrap lives in {@link ProxyPlatformInitializer}.
 */
public final class VelocityPermissionsExPlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private DefaultPermissionManager manager;
    private PlatformRuntime platformRuntime;

    @Inject
    public VelocityPermissionsExPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    public ProxyServer server() {
        return server;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) throws PermissionBackendException {
        var adapter = new VelocityPlatformAdapter(server);
        var scheduler = new VelocityPlatformScheduler(this, server);
        platformRuntime = PlatformRuntime.of(adapter, NoOpPlatformEventBus.INSTANCE, scheduler);
        manager = ProxyPlatformInitializer.start(
                        dataDirectory.toFile(),
                        java.util.logging.Logger.getLogger(logger.getName()),
                        platformRuntime)
                .manager();
        VelocityPermissionBootstrapReporter.log(this, manager, logger);
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
        ProxyPlatformInitializer.shutdown(manager);
        manager = null;
        platformRuntime = null;
        logger.info("PermissionsExPlus Velocity adapter disabled");
    }

    public PermissionManager manager() {
        return manager;
    }
}
