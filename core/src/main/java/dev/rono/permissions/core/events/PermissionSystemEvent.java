package dev.rono.permissions.core.events;

import java.util.UUID;

public class PermissionSystemEvent extends PermissionEvent {
    protected Action action;

    public PermissionSystemEvent(UUID sourceUUID, Action action) {
        super(sourceUUID);
        this.action = action;
    }

    public Action getAction() {
        return this.action;
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
