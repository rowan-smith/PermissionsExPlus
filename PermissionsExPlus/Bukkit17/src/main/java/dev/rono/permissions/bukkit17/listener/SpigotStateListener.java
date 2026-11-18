package dev.rono.permissions.bukkit17.listener;

import lombok.var;

import dev.rono.permissions.core.context.CoreStateTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public final class SpigotStateListener implements Listener {
    private final CoreStateTracker tracker;
    private final Consumer<Player> refreshPermissions;

    public SpigotStateListener(CoreStateTracker tracker, Consumer<Player> refreshPermissions) {
        this.tracker = Objects.requireNonNull(tracker, "tracker");
        this.refreshPermissions = Objects.requireNonNull(refreshPermissions, "refreshPermissions");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        track(player);

        refreshPermissions.accept(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        var player = event.getPlayer();

        tracker.updateState(player.getUniqueId(), "world", player.getWorld().getName());

        refreshPermissions.accept(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        var player = event.getPlayer();

        tracker.updateState(player.getUniqueId(), "gamemode", event.getNewGameMode().name().toLowerCase(Locale.ROOT));

        refreshPermissions.accept(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        tracker.clearState(event.getPlayer().getUniqueId());
    }

    public void track(Player player) {
        tracker.updateState(player.getUniqueId(), "world", player.getWorld().getName());
        tracker.updateState(player.getUniqueId(), "gamemode", player.getGameMode().name().toLowerCase(Locale.ROOT));
    }
}
