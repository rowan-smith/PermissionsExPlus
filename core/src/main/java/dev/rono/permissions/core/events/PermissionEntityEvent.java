package dev.rono.permissions.core.events;

import java.util.UUID;

public class PermissionEntityEvent extends PermissionEvent {
    protected Action action;
    protected String type;
    protected String entityIdentifier;

    public PermissionEntityEvent(UUID sourceUUID, String entityIdentifier, String type, Action action) {
        super(sourceUUID);
        this.entityIdentifier = entityIdentifier;
        this.type = type;
        this.action = action;
    }

    public Action getAction() {
        return this.action;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public String getEntityType() {
        return type;
    }

    public Object getEntity() {
        return null;
    }

    public enum Action {
        PERMISSIONS_CHANGED,
        OPTIONS_CHANGED,
        INHERITANCE_CHANGED,
        INFO_CHANGED,
        TIMEDPERMISSION_EXPIRED,
        RANK_CHANGED,
        DEFAULTGROUP_CHANGED,
        WEIGHT_CHANGED,
        SAVED,
        REMOVED,
    }
}
