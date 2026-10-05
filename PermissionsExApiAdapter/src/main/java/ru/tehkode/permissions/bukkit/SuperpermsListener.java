package ru.tehkode.permissions.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionSystemEvent;

/**
 * ABI shell. Superperms attachment injection is owned by PermissionsExPlus.
 */
public class SuperpermsListener implements Listener {
    public SuperpermsListener(PermissionsEx plugin) {}

    protected void updateAttachment(Player player) {}

    protected void updateAttachment(Player player, String worldName) {}

    protected void removeAttachment(Player player) {}

    public void onDisable() {}

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {}

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {}

    @EventHandler
    public void onPlayerLoginLate(PlayerLoginEvent event) {}

    @EventHandler
    public void playerLoginDeny(PlayerLoginEvent event) {}

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {}

    @EventHandler
    public void onEntityEvent(PermissionEntityEvent event) {}

    @EventHandler
    public void onWorldChanged(PlayerChangedWorldEvent event) {}

    @EventHandler
    public void onSystemEvent(PermissionSystemEvent event) {}
}
