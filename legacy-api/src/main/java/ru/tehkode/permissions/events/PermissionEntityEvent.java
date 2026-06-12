package ru.tehkode.permissions.events;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionManager;

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
            PermissionManager manager = resolveManager();
            if (manager != null) {
                switch (type) {
                    case GROUP:
                        entity = manager.getGroup(entityIdentifier);
                        break;
                    case USER:
                        entity = manager.getUser(entityIdentifier);
                        break;
                    default:
                        break;
                }
            }
        }
        return entity;
    }

    private static PermissionManager resolveManager() {
        RegisteredServiceProvider<PermissionManager> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        return reg == null ? null : reg.getProvider();
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
