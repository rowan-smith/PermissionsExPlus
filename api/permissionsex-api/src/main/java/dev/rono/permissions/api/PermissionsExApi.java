package dev.rono.permissions.api;

import dev.rono.permissions.api.event.PermissionEventBus;
import dev.rono.permissions.api.group.GroupManager;
import dev.rono.permissions.api.ladder.LadderManager;
import dev.rono.permissions.api.user.UserManager;
import dev.rono.permissions.api.world.WorldManager;
import ru.tehkode.permissions.PermissionManager;

/**
 * Primary PermissionsEx hook API entry surface.
 *
 * <p>Resolve via {@code ru.tehkode.permissions.bukkit.PermissionsEx.getApi()} on Spigot/Paper,
 * or {@code dev.rono.permissions.bungee.PermissionsEx.getApi()} on Bungee/Waterfall.</p>
 *
 * <p><strong>API layering:</strong> new plugins should use {@code PermissionsExApi} and its managers
 * ({@link #getUserManager()}, {@link #getGroupManager()}, …). {@link #getPermissionManager()} exposes
 * the classic/holder bridge for context-aware checks and legacy interop — not as a second primary API.
 * Subject operations ({@code User} / {@code Group}) are convenience facades over the same engine.</p>
 *
 * @see dev.rono.permissions.api.subject.PermissionSubject
 */
public interface PermissionsExApi {

    /**
     * Returns the user registry for find/get/create lifecycle operations.
     *
     * @return shared {@link dev.rono.permissions.api.user.UserManager}
     */
    UserManager getUserManager();

    /**
     * Returns the group registry for find/get/create lifecycle operations.
     *
     * @return shared {@link dev.rono.permissions.api.group.GroupManager}
     */
    GroupManager getGroupManager();

    /**
     * Returns the world registry for permission namespace management.
     *
     * @return shared {@link dev.rono.permissions.api.world.WorldManager}
     */
    WorldManager getWorldManager();

    /**
     * Returns the rank-ladder registry and promotion/demotion operations.
     *
     * @return shared {@link dev.rono.permissions.api.ladder.LadderManager}
     */
    LadderManager getLadderManager();

    /**
     * Returns the permission-domain event bus for subscribing to entity and system dispatches.
     *
     * @return shared {@link PermissionEventBus}
     */
    PermissionEventBus getEventBus();

    /**
     * Classic and holder-based permission operations ({@link ru.tehkode.permissions.PermissionUser},
     * {@link ru.tehkode.permissions.PermissionGroup}, {@link dev.rono.permissions.api.permission.PermissionHolder}, etc.).
     */
    PermissionManager getPermissionManager();
}
