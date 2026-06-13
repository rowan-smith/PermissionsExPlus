package dev.rono.proxychat.exampleLegacyPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.Arrays;
import java.util.Locale;

/**
 * Sample plugin compiling only against {@code permissionsex-legacy-api} (+ {@code spigot-api}): classic static
 * {@link PermissionsEx} entry points and {@link PermissionManager} operations.
 */
@SuppressWarnings("deprecation")
public class ExampleLegacyPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        if (!PermissionsEx.isAvailable()) {
            getLogger().warning("PermissionsEx is not loaded or not enabled.");
            return;
        }

        try {
            var permissionManager = PermissionsEx.getPermissionManager();
            getLogger().info("PEX backend: " + permissionManager.getBackend().getClass().getSimpleName());

        } catch (Throwable t) {
            getLogger().severe("PEX is enabled but PermissionManager is not available yet.");
            t.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        if (!PermissionsEx.isAvailable()) {
            return;
        }

        try {
            var user = PermissionsEx.getUser(player);
            var mgr = PermissionsEx.getPermissionManager();
            var allowed = mgr.has(player.getUniqueId(), "my.node", player.getWorld().getName());
            var lastKnownName = user.getOption("name");
            var id = player.getUniqueId();

            getLogger().fine(() -> String.format(Locale.ROOT,
                    "pex uuid=%s user=%s allowed(my.node)=%s option(name)=%s parents=%s",
                    id,
                    player.getName(),
                    allowed,
                    lastKnownName,
                    Arrays.toString(user.getGroupsNames())));

        } catch (Throwable t) {
            getLogger().fine(() -> "PEX API not reachable on join: " + t.getMessage());
        }
    }
}
