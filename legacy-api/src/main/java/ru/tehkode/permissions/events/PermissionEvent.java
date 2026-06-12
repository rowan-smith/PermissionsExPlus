package ru.tehkode.permissions.events;

import java.io.Serializable;
import java.util.UUID;
import org.bukkit.event.Event;

/** Classic PEX Bukkit-rooted notification. */
public abstract class PermissionEvent extends Event implements Serializable {
    private final UUID serverId;

    public PermissionEvent(UUID id) {
        serverId = id;
    }

    public UUID getSourceUUID() {
        return serverId;
    }
}
