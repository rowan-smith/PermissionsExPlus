package dev.rono.permissions.example;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.bukkit.BukkitPermissions;
import java.util.Locale;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Sample plugin compiling only against {@code permissionsex-api} (+ {@code spigot-api}): modern
 * {@link PermissionService} on Bukkit {@code ServicesManager}.
 */
public class ExamplePlugin extends JavaPlugin implements Listener {

    private PermissionService permissions;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        RegisteredServiceProvider<PermissionService> registration =
                getServer().getServicesManager().getRegistration(PermissionService.class);
        if (registration == null) {
            getLogger().warning("PermissionService is not registered — is PermissionsEx loaded?");
            return;
        }

        permissions = registration.getProvider();
        getLogger().info(String.format(Locale.ROOT,
                "PEX backend: %s (%s), users=%d groups=%d",
                permissions.backend().type(),
                permissions.backend().simpleName(),
                permissions.userCount(),
                permissions.groupCount()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        if (permissions == null) {
            return;
        }

        Player player = event.getPlayer();
        String world = player.getWorld().getName();
        boolean allowed = BukkitPermissions.has(permissions, player, "my.node");

        User user = permissions.user(player.getUniqueId());
        var worldContext = user.inWorld(world);
        String displayName = worldContext.option("name");
        if (displayName == null) {
            displayName = player.getName();
        }
        final String resolvedName = displayName;

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
