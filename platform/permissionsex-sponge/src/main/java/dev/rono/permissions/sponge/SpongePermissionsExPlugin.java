package dev.rono.permissions.sponge;

import com.google.inject.Inject;
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
import dev.rono.permissions.runtime.startup.SpongePermissionBootstrapReporter;
import dev.rono.permissions.sponge.platform.SpongePlatformAdapter;
import dev.rono.permissions.sponge.platform.SpongePlatformScheduler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * Sponge runtime entry point. Engine bootstrap reuses {@link ProxyPlatformInitializer} for config/backends/API registry.
 */
@Plugin("permissionsex")
public final class SpongePermissionsExPlugin implements PermissionsEx.ProxyLegacyBridgeHost {
    private final PluginContainer container;
    private final Logger logger;
    private final Path configDir;
    private DefaultPermissionManager manager;
    private BungeePermissionsExConfig config;
    private PlatformRuntime platformRuntime;
    private CoreCommandService commandService;
    private SpongeCloudCommandManager<CommandCause> cloudManager;
    private ProxyLegacyBridgeController legacyBridge;

    @Inject
    public SpongePermissionsExPlugin(PluginContainer container, Logger logger, @ConfigDir(sharedRoot = false) Path configDir) {
        this.container = container;
        this.logger = logger;
        this.configDir = configDir;
    }

    @Listener
    public void onEngineStart(StartedEngineEvent<Server> event) throws PermissionBackendException {
        Server server = event.engine();
        var adapter = new SpongePlatformAdapter(server);
        var scheduler = new SpongePlatformScheduler(container, server);
        platformRuntime = PlatformRuntime.of(adapter, NoOpPlatformEventBus.INSTANCE, scheduler);
        var startup = ProxyPlatformInitializer.start(
                configDir.toFile(),
                java.util.logging.Logger.getLogger(logger.getName()),
                platformRuntime);
        config = startup.config();
        manager = startup.manager();
        legacyBridge = startup.legacyBridge();
        PermissionsEx.registerBridgeHost(this);
        maybeActivateLegacyBridge(server, "startup scan");
        cloudManager = new SpongeCloudCommandManager<>(
                container,
                cloud.commandframework.execution.CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity());
        SpongePermissionBootstrapReporter.log(this, manager, server, logger, legacyBridge.isActive());
    }

    @Override
    public void ensureLegacyBridgeForHook(String reason) {
        if (legacyBridge != null) {
            legacyBridge.activate(reason, java.util.logging.Logger.getLogger(logger.getName()));
        }
    }

    private void maybeActivateLegacyBridge(Server server, String scanContext) {
        var hook = SpongeLegacyHookPluginDetector.findHook(server.game().pluginManager().plugins(), container);
        if (hook != null) {
            legacyBridge.activate(
                    "detected hook plugin '" + hook.name() + "' (" + scanContext + ")",
                    java.util.logging.Logger.getLogger(logger.getName()));
        }
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<org.spongepowered.api.command.Command.Raw> event) {
        if (cloudManager == null || manager == null || config == null) {
            return;
        }
        if (commandService == null) {
            commandService = PexCloudCommands.install(new PexCloudCommands.InstallRequest<>(
                    cloudManager,
                    CommandCause.class,
                    manager,
                    new SpongeSenderAdapter(),
                    config::reload,
                    new ProxyConfigBridge(config),
                    force -> "UUID conversion is not supported on Sponge.",
                    CoreCloudPlatform.PROXY,
                    config.options().current().commandFramework()));
        }
        cloudManager.registerQueuedCommands(event);
    }

    @Listener
    public void onEngineStop(StoppingEngineEvent<Server> event) {
        PermissionsEx.clearBridgeHost();
        ProxyPlatformInitializer.shutdown(manager);
        manager = null;
        legacyBridge = null;
        config = null;
        commandService = null;
        cloudManager = null;
        platformRuntime = null;
        logger.info("PermissionsExPlus Sponge adapter disabled");
    }

    public PermissionManager manager() {
        return manager;
    }

    private final class SpongeSenderAdapter implements CoreCloudCommandRegistrar.SenderAdapter<CommandCause> {
        @Override
        public void reply(CommandCause sender, String message) {
            sender.audience().sendMessage(net.kyori.adventure.text.Component.text(message));
        }

        @Override
        public String defaultWorld(CommandCause sender) {
            if (sender.root() instanceof ServerPlayer player) {
                return player.world().key().asString();
            }
            return null;
        }

        @Override
        public PermissionUser actor(CommandCause sender) {
            if (sender.root() instanceof ServerPlayer player) {
                return manager.getUser(player.uniqueId());
            }
            return null;
        }

        @Override
        public String helpText() {
            return "PermissionsExPlus commands loaded. Use /pex help.";
        }

        @Override
        public String pluginVersion() {
            return String.valueOf(container.metadata().version());
        }
    }
}
