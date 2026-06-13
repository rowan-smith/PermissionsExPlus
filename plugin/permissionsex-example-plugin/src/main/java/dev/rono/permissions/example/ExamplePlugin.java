package dev.rono.permissions.example;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;

import java.util.Locale;
import dev.rono.permissions.api.permission.PermissionContext;

/** Sample plugin using {@link PermissionsEx#getApi()}. */
public class ExamplePlugin extends JavaPlugin implements Listener {

    private PermissionManager permissions;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        if (!PermissionsEx.isAvailable()) {
            getLogger().warning("PermissionsEx is not available — is PermissionsEx loaded?");
            return;
        }

        permissions = PermissionsEx.getApi().getPermissionManager();
        getLogger().info(String.format(Locale.ROOT,
                "PEX users=%d groups=%d",
                PermissionsEx.getApi().getUserManager().count(),
                PermissionsEx.getApi().getGroupManager().count()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        if (permissions == null) {
            return;
        }

        var player = event.getPlayer();
        var api = PermissionsEx.getApi();
        var user = api.getUserManager().getUser(player.getUniqueId());
        var worldName = player.getWorld().getName();

        var context = PermissionContext.of(worldName, getServer().getName(), "spawn", player.getGameMode().name());
        var allowed = permissions.hasPermission(user.asHolder(), "my.node", context);
        var worldContext = user.inWorld(worldName);
        var displayName = worldContext.option("name");

        if (displayName == null) {
            displayName = player.getName();
        }

        final var resolvedName = displayName;

        getLogger().fine(() -> String.format(Locale.ROOT,
                "pex uuid=%s user=%s allowed(my.node)=%s groups=%s directPerms=%s timed=%s",
                player.getUniqueId(),
                resolvedName,
                allowed,
                worldContext.groups(),
                worldContext.permissions(),
                worldContext.timedPermissionEntries()));
    }
}
