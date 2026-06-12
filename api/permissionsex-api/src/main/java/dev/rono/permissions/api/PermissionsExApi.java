package dev.rono.permissions.api;

import dev.rono.permissions.api.group.GroupManager;
import dev.rono.permissions.api.ladder.LadderManager;
import dev.rono.permissions.api.permission.PermissionService;
import dev.rono.permissions.api.user.UserManager;
import dev.rono.permissions.api.world.WorldManager;
import ru.tehkode.permissions.PermissionManager;

/**
 * Modern PermissionsEx hook API entry surface.
 *
 * <p>Resolve via {@link ru.tehkode.permissions.bukkit.PermissionsEx#getApi()} on Spigot/Paper,
 * or {@link dev.rono.permissions.bungee.PermissionsEx#getApi()} on Bungee/Waterfall.</p>
 */
public interface PermissionsExApi {

    UserManager getUserManager();

    GroupManager getGroupManager();

    WorldManager getWorldManager();

    LadderManager getLadderManager();

    /**
     * Holder-based permission operations (add / remove / has).
     */
    PermissionService getPermissionService();

    /**
     * Classic {@code ru.tehkode.permissions} manager ({@link ru.tehkode.permissions.PermissionUser},
     * {@link ru.tehkode.permissions.PermissionGroup}, etc.).
     */
    PermissionManager getPermissionManager();
}
