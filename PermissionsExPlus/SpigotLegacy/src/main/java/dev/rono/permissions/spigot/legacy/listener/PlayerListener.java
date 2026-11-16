package dev.rono.permissions.spigot.legacy.listener;

import lombok.var;

import dev.rono.permissions.core.PexApiImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {
    private final PexApiImpl<?> core;

    public PlayerListener(PexApiImpl<?> core) {
        this.core = core;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!core.config().preloadOnJoin() || event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        try {
            core.users().loadOrCreateUser(event.getUniqueId(), event.getName()).toCompletableFuture().join();
        } catch (RuntimeException ignored) {}
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        core.users().cache().markOnline(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();

        core.users().cache().markOffline(uuid);
    }
}
