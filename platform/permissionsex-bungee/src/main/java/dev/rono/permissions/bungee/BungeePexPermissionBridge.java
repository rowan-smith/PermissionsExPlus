package dev.rono.permissions.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

/**
 * Applies PEX to BungeeCord's proxy permission pipeline. The event seeds {@link ProxiedPlayer#hasPermission(String)}
 * with Bungee's own permission snapshot; when PEX has a matching permission expression for this check we override
 * the outcome. Absent expressions leave whatever other plugins / Bungee already set.
 */
public final class BungeePexPermissionBridge implements Listener {
    private final PermissionManager manager;

    public BungeePexPermissionBridge(PermissionManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPermissionCheck(PermissionCheckEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer player)) {
            return;
        }
        PermissionUser user = manager.getUser(player.getUniqueId());
        String permission = event.getPermission();
        String world =
                player.getServer() != null ? player.getServer().getInfo().getName() : null;
        String expression = user.getMatchingExpression(permission, world);
        if (expression == null) {
            return;
        }
        event.setHasPermission(user.explainExpression(expression));
    }
}
