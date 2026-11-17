package dev.rono.permissions.bungee;

import dev.rono.permissions.api.PexRegistration;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexApiImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class BungeePlugin extends Plugin implements Listener {
    private PexApiImpl<CommandSender> core;

    @Override
    public void onEnable() {
        try {
            core = new PexApiImpl<>(new BungeePlatform(this));
            core.start();
        } catch (RuntimeException error) {
            getLogger().log(Level.SEVERE, "Unable to initialize PermissionsExPlus", error);
            core = null;
            return;
        }

        core.contexts().registry().registerContextType("server", this::serverNames);
        getProxy().getPluginManager().registerListener(this, this);

        PexRegistration.register(core);
    }

    private List<String> serverNames() {
        return new ArrayList<>(getProxy().getServers().keySet());
    }

    @Override
    public void onDisable() {
        if (core == null) {
            return;
        }

        PexRegistration.unregister(core);
        core.stop();
    }

    /**
     * BungeeCord asks plugins to override its own (config.yml) answer. Players are
     * answered by PermissionsExPlus; when it has no opinion, Bungee's answer
     * stands.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPermissionCheck(PermissionCheckEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        PermissionResult result = resolve((ProxiedPlayer) event.getSender(), event.getPermission());

        if (result != PermissionResult.UNDEFINED) {
            event.setHasPermission(result == PermissionResult.ALLOW);
        }
    }

    private PermissionResult resolve(ProxiedPlayer player, String permission) {
        UUID id = player.getUniqueId();
        Optional<User> cached = core.users().cache().get(id);

        if (cached.isPresent()) {
            return core.resolvers().permissions().check(cached.get(), permission, core.contexts().queryOptions(id));
        }

        core.users().loadOrCreateUserAsync(id, player.getName(), user -> {});

        return core.cacheFailureFallback(id, permission);
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        if (!core.config().preloadOnJoin() || event.isCancelled()) {
            return;
        }

        UUID id = event.getConnection().getUniqueId();

        // LoginEvent may block: Bungee runs it off the network thread when registered
        // as an intent.
        event.registerIntent(this);
        core.users().loadOrCreateUser(id, event.getConnection().getName()).whenComplete((user, error) -> event.completeIntent(this));
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        core.users().cache().markOnline(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        ServerInfo server = event.getServer().getInfo();

        core.stateTracker().updateState(event.getPlayer().getUniqueId(), "server", server.getName());
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        UUID id = event.getPlayer().getUniqueId();

        core.users().cache().markOffline(id);
        core.stateTracker().clearState(id);
    }
}
