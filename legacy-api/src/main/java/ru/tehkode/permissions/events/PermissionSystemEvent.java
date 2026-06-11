package ru.tehkode.permissions.events;

import java.util.UUID;
import org.bukkit.event.HandlerList;

public class PermissionSystemEvent extends PermissionEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    protected Action action;

    public PermissionSystemEvent(UUID sourceUUID, Action action) {
        super(sourceUUID);
        this.action = action;
    }

    public Action getAction() {
        return this.action;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum Action {
        BACKEND_CHANGED,
        RELOADED,
        WORLDINHERITANCE_CHANGED,
        DEFAULTGROUP_CHANGED,
        DEBUGMODE_TOGGLE,
        REINJECT_PERMISSIBLES,
    }
}
