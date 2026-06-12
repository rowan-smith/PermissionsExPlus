package ru.tehkode.permissions.events;

import java.io.Serializable;
import java.util.UUID;
import org.bukkit.event.Event;

/**
 * Base class for classic PermissionsEx Bukkit-rooted notifications.
 *
 * <p>All permission events carry the UUID of the server instance that originated the change, allowing
 * network listeners to ignore or handle events from remote servers appropriately.</p>
 */
public abstract class PermissionEvent extends Event implements Serializable {
    private final UUID serverId;

    /**
     * Creates a permission event originating from the given server.
     *
     * @param id UUID identifying the server that triggered this event; must not be {@code null}
     */
    public PermissionEvent(UUID id) {
        serverId = id;
    }

    /**
     * Returns the UUID of the server that originated this event.
     *
     * @return source server UUID; never {@code null}
     */
    public UUID getSourceUUID() {
        return serverId;
    }
}
