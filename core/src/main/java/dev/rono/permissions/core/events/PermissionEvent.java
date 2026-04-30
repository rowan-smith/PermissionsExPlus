package dev.rono.permissions.core.events;

import java.io.Serializable;
import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Classic PEX Bukkit-rooted notification (1.23.x-compatible shape). */
public abstract class PermissionEvent extends Event implements Serializable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID serverId;

    public PermissionEvent(UUID id) {
        super(false);
        serverId = id;
    }

    public UUID getSourceUUID() {
        return serverId;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
