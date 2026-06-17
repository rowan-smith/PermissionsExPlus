package dev.rono.permissions.velocity;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.rono.permissions.api.runtime.NoOpPlatformEventBus;
import dev.rono.permissions.api.runtime.PlatformRuntime;
import dev.rono.permissions.bungee.BungeePermissionsExConfig;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.commands.CoreCloudCommandRegistrar;
import dev.rono.permissions.core.commands.CoreCloudPlatform;
import dev.rono.permissions.core.commands.CoreCommandService;
import dev.rono.permissions.core.commands.PexCloudCommands;
import dev.rono.permissions.proxy.commands.ProxyConfigBridge;
import dev.rono.permissions.bungee.PermissionsEx;
import dev.rono.permissions.runtime.legacy.ProxyLegacyBridgeController;
import dev.rono.permissions.runtime.startup.ProxyPlatformInitializer;
import dev.rono.permissions.runtime.startup.VelocityPermissionBootstrapReporter;
import dev.rono.permissions.velocity.platform.VelocityPlatformAdapter;
import dev.rono.permissions.velocity.platform.VelocityPlatformScheduler;
import org.slf4j.Logger;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * Velocity proxy entry point. Shared proxy bootstrap lives in {@link ProxyPlatformInitializer}.
 */
public final class VelocityPermissionsExPlugin implements PermissionsEx.ProxyLegacyBridgeHost {
    private final ProxyServer server;
    private final PluginContainer pluginContainer;
    private final Logger logger;
    private final Path dataDirectory;
    private DefaultPermissionManager manager;
    private BungeePermissionsExConfig config;
    private PlatformRuntime platformRuntime;
    private CoreCommandService commandService;
    private StrippingVelocityCommandManager<CommandSource> cloudManager;
    private ProxyLegacyBridgeController legacyBridge;

    @Inject
    public VelocityPermissionsExPlugin(
            ProxyServer server,
            PluginContainer pluginContainer,
            Logger logger,
            @DataDirectory Path dataDirectory) {
        this.server = server;
        this.pluginContainer = pluginContainer;
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
        var startup = ProxyPlatformInitializer.start(
                dataDirectory.toFile(),
                java.util.logging.Logger.getLogger(logger.getName()),
                platformRuntime);
        config = startup.config();
        manager = startup.manager();
        legacyBridge = startup.legacyBridge();
        PermissionsEx.registerBridgeHost(this);
        maybeActivateLegacyBridge("startup scan");
        cloudManager = new StrippingVelocityCommandManager<>(
                pluginContainer,
                server,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity());
        commandService = PexCloudCommands.install(new PexCloudCommands.InstallRequest<>(
                cloudManager,
                CommandSource.class,
                manager,
                new VelocitySenderAdapter(),
                config::reload,
                new ProxyConfigBridge(config),
                force -> "UUID conversion is not supported on Velocity.",
                CoreCloudPlatform.PROXY,
                config.options().current().commandFramework()));
        VelocityPermissionBootstrapReporter.log(this, manager, logger, legacyBridge.isActive());
    }

    @Override
    public void ensureLegacyBridgeForHook(String reason) {
        if (legacyBridge != null) {
            legacyBridge.activate(reason, java.util.logging.Logger.getLogger(logger.getName()));
        }
    }

    private void maybeActivateLegacyBridge(String scanContext) {
        var hook = VelocityLegacyHookPluginDetector.findHook(server.getPluginManager(), pluginContainer);
        if (hook != null) {
            legacyBridge.activate(
                    "detected hook plugin '" + hook.name() + "' (" + scanContext + ")",
                    java.util.logging.Logger.getLogger(logger.getName()));
        }
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
        PermissionsEx.clearBridgeHost();
        ProxyPlatformInitializer.shutdown(manager);
        manager = null;
        legacyBridge = null;
        config = null;
        commandService = null;
        cloudManager = null;
        platformRuntime = null;
        logger.info("PermissionsExPlus Velocity adapter disabled");
    }

    public PermissionManager manager() {
        return manager;
    }

    private final class VelocitySenderAdapter implements CoreCloudCommandRegistrar.SenderAdapter<CommandSource> {
        @Override
        public void reply(CommandSource sender, String message) {
            sender.sendMessage(net.kyori.adventure.text.Component.text(message));
        }

        @Override
        public String defaultWorld(CommandSource sender) {
            if (sender instanceof Player player && player.getCurrentServer().isPresent()) {
                return player.getCurrentServer().get().getServerInfo().getName();
            }
            return null;
        }

        @Override
        public PermissionUser actor(CommandSource sender) {
            if (sender instanceof Player player) {
                return manager.getUser(player.getUniqueId());
            }
            return null;
        }

        @Override
        public String helpText() {
            return "PermissionsExPlus commands loaded. Use /pex help.";
        }

        @Override
        public String pluginVersion() {
            return pluginContainer.getDescription().getVersion().orElse("unknown");
        }
    }
}
