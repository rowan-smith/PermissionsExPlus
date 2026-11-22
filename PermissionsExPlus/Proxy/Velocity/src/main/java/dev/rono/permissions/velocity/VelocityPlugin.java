package dev.rono.permissions.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.rono.permissions.api.PexRegistration;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexApiImpl;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;

public final class VelocityPlugin {
    private final ProxyServer proxy;
    private final PluginContainer container;
    private final Logger logger;
    private final Path dataDirectory;

    private PexApiImpl<CommandSource> core;

    @Inject
    public VelocityPlugin(ProxyServer proxy, PluginContainer container, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.container = container;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onInitialize(ProxyInitializeEvent event) {
        core = new PexApiImpl<>(new VelocityPlatform(proxy, container, this, logger, dataDirectory));
        core.start();

        core.contexts().registry().registerContextType("server", this::serverNames);

        PexRegistration.register(core);
    }

    private List<String> serverNames() {
        List<String> names = new ArrayList<>();

        for (RegisteredServer server : proxy.getAllServers()) {
            names.add(server.getServerInfo().getName());
        }

        return names;
    }

    /**
     * Players are answered by PermissionsExPlus; the console keeps Velocity's
     * default.
     */
    @Subscribe
    public void onPermissionsSetup(PermissionsSetupEvent event) {
        if (!(event.getSubject() instanceof Player) || core == null) {
            return;
        }

        Player player = (Player) event.getSubject();

        event.setProvider(subject -> (PermissionFunction) permission -> tristate(resolve(player, permission)));
    }

    private PermissionResult resolve(Player player, String permission) {
        UUID id = player.getUniqueId();
        Optional<User> cached = core.users().cache().get(id);

        if (cached.isPresent()) {
            return core.resolvers().permissions().check(cached.get(), permission, core.contexts().queryOptions(id));
        }

        core.users().loadOrCreateUserAsync(id, player.getUsername(), user -> {});

        return core.cacheFailureFallback(id, permission);
    }

    private static Tristate tristate(PermissionResult result) {
        switch (result) {
            case ALLOW :
                return Tristate.TRUE;
            case DENY :
                return Tristate.FALSE;
            default :
                return Tristate.UNDEFINED;
        }
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onLogin(LoginEvent event) {
        if (core == null || !core.config().preloadOnJoin()) {
            return;
        }

        try {
            core.users().loadOrCreateUser(event.getPlayer().getUniqueId(), event.getPlayer().getUsername()).toCompletableFuture().join();
        } catch (RuntimeException ignored) {
            // Loaded lazily on first permission check instead.
        }
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        core.users().cache().markOnline(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        core.stateTracker().updateState(event.getPlayer().getUniqueId(), "server", event.getServer().getServerInfo().getName());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        UUID id = event.getPlayer().getUniqueId();

        core.users().cache().markOffline(id);
        core.stateTracker().clearState(id);
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        if (core == null) {
            return;
        }

        PexRegistration.unregister(core);
        core.stop();
    }
}
