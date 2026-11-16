package dev.rono.permissions.sponge8;

import com.google.inject.Inject;
import dev.rono.permissions.api.PexRegistration;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.sponge8.platform.Sponge8Platform;
import dev.rono.permissions.sponge8.service.PexPermissionService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.ProvideServiceEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("permissionsexplus")
public final class Sponge8Plugin {
    private final PluginContainer container;
    private final Logger logger;
    private final Path configDirectory;

    private Sponge8Platform platform;
    private PexApiImpl<CommandCause> core;
    private PexPermissionService service;

    @Inject
    public Sponge8Plugin(PluginContainer container, Logger logger, @ConfigDir(sharedRoot = false) Path configDirectory) {
        this.container = container;
        this.logger = logger;
        this.configDirectory = configDirectory;
    }

    /**
     * Starts PermissionsExPlus once, on whichever lifecycle event needs it first,
     * so
     * the permission service and command roots exist regardless of event order.
     */
    private synchronized PexApiImpl<CommandCause> core() {
        if (core == null) {
            platform = new Sponge8Platform(container, logger, configDirectory);
            core = new PexApiImpl<>(platform);
            core.start();
            service = new PexPermissionService(core);

            core.contexts().registry().registerContextType("world", Sponge8Plugin::worldNames);

            PexRegistration.register(core);
        }

        return core;
    }

    private static List<String> worldNames() {
        List<String> names = new ArrayList<>();

        if (Sponge.isServerAvailable()) {
            for (ServerWorld world : Sponge.server().worldManager().worlds()) {
                names.add(world.key().asString());
            }
        }

        return names;
    }

    @Listener(order = Order.FIRST)
    public void onConstruct(ConstructPluginEvent event) {
        core();
    }

    @Listener
    public void onProvidePermissionService(ProvideServiceEvent.EngineScoped<PermissionService> event) {
        core();
        event.suggest(() -> service);
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Command.Raw> event) {
        core();
        platform.commands().register(event);
    }

    @Listener(order = Order.EARLY)
    public void onAuth(ServerSideConnectionEvent.Auth event) {
        if (!core().config().preloadOnJoin()) {
            return;
        }

        UUID id = event.profile().uniqueId();

        try {
            core.users().loadOrCreateUser(id, event.profile().name().orElse(id.toString())).toCompletableFuture().join();
        } catch (RuntimeException ignored) {
            // Loaded lazily on first permission check instead.
        }
    }

    @Listener
    public void onJoin(ServerSideConnectionEvent.Join event) {
        ServerPlayer player = event.player();
        UUID id = player.uniqueId();

        core().users().cache().markOnline(id);
        core.stateTracker().updateState(id, "world", player.world().key().asString());
    }

    @Listener
    public void onDisconnect(ServerSideConnectionEvent.Disconnect event) {
        UUID id = event.player().uniqueId();

        core().users().cache().markOffline(id);
        core.stateTracker().clearState(id);
    }

    @Listener
    public void onStopping(StoppingEngineEvent<Server> event) {
        if (core == null) {
            return;
        }

        PexRegistration.unregister(core);
        core.stop();
    }
}
