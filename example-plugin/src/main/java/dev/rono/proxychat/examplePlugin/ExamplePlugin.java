package dev.rono.proxychat.examplePlugin;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Sample plugin compiling only against {@code permissionsex-legacy-api} (+ {@code spigot-api}): classic static
 * {@link PermissionsEx} entry points and {@link PermissionManager} operations.
 */
public final class ExamplePlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        if (!PermissionsEx.isAvailable()) {
            getLogger().warning("PermissionsEx is not loaded or not enabled.");
            return;
        }
        try {
            PermissionManager mgr = PermissionsEx.getPermissionManager();
            getLogger().info("PEX backend: " + mgr.getBackend().getClass().getSimpleName());
        } catch (Throwable t) {
            getLogger().severe("PEX is enabled but PermissionManager is not available yet.");
            t.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!PermissionsEx.isAvailable()) {
            return;
        }
        try {
            PermissionUser user = PermissionsEx.getUser(player);
            PermissionManager mgr = PermissionsEx.getPermissionManager();
            boolean allowed = mgr.has(player.getUniqueId(), "my.node", player.getWorld().getName());
            String lastKnownName = user.getOption("name");
            UUID id = player.getUniqueId();

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
