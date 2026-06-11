package ru.tehkode.permissions.events;

import java.util.UUID;
import org.bukkit.event.HandlerList;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionEntityEvent extends PermissionEvent {
    private static final HandlerList handlers = new HandlerList();
    protected transient PermissionEntity entity;
    protected Action action;
    protected PermissionEntity.Type type;
    protected String entityIdentifier;

    public PermissionEntityEvent(UUID sourceUUID, PermissionEntity entity, Action action) {
        super(sourceUUID);
        this.entity = entity;
        this.entityIdentifier = entity.getIdentifier();
        this.type = entity.getType();
        this.action = action;
    }

    public Action getAction() {
        return this.action;
    }

    public PermissionEntity getEntity() {
        if (entity == null) {
            switch (type) {
                case GROUP:
                    entity = PermissionsEx.getPermissionManager().getGroup(entityIdentifier);
                    break;
                case USER:
                    entity = PermissionsEx.getPermissionManager().getUser(entityIdentifier);
                    break;
                default:
                    break;
            }
        }
        return entity;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public PermissionEntity.Type getType() {
        return type;
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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
