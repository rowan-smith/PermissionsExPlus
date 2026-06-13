package ru.tehkode.permissions.events;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionManager;

import java.util.UUID;

/**
 * Bukkit event fired when a permission user or group is created, modified, or removed.
 *
 * <p>Listeners receive the affected entity (or its identifier when deserialized), the entity type,
 * and the kind of change that occurred. The entity reference may be lazily re-resolved from the
 * {@link PermissionManager} if it was not available at construction time.</p>
 */
public class PermissionEntityEvent extends PermissionEvent {
    private static final HandlerList handlers = new HandlerList();
    protected transient PermissionEntity entity;
    protected Action action;
    protected PermissionEntity.Type type;
    protected String entityIdentifier;

    /**
     * Creates an entity change event.
     *
     * @param sourceUUID UUID of the server that originated this event; must not be {@code null}
     * @param entity     affected permission entity; must not be {@code null}
     * @param action     kind of change that occurred; must not be {@code null}
     */
    public PermissionEntityEvent(UUID sourceUUID, PermissionEntity entity, Action action) {
        super(sourceUUID);
        this.entity = entity;
        this.entityIdentifier = entity.getIdentifier();
        this.type = entity.getType();
        this.action = action;
    }

    /**
     * Returns the kind of change that triggered this event.
     *
     * @return action descriptor; never {@code null}
     */
    public Action getAction() {
        return this.action;
    }

    /**
     * Returns the permission entity affected by this event.
     *
     * <p>If the entity reference was not available (for example after deserialization), it is
     * re-resolved from the registered {@link PermissionManager} using {@link #getEntityIdentifier()}
     * and {@link #getType()}.</p>
     *
     * @return affected entity, or {@code null} if it cannot be resolved
     */
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

    /**
     * Returns the identifier of the affected entity.
     *
     * @return entity identifier (user name/UUID or group name); never {@code null}
     */
    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    /**
     * Returns whether the affected entity is a user or a group.
     *
     * @return entity type; never {@code null}
     */
    public PermissionEntity.Type getType() {
        return type;
    }

    /**
     * Describes the kind of change applied to a permission entity.
     */
    public enum Action {
        /** Direct permission nodes on the entity were added, removed, or changed. */
        PERMISSIONS_CHANGED,
        /** Prefix, suffix, or other metadata options were changed. */
        OPTIONS_CHANGED,
        /** Parent group inheritance was changed. */
        INHERITANCE_CHANGED,
        /** General entity information (such as display name) was changed. */
        INFO_CHANGED,
        /** A timed (temporary) permission expired and was removed. */
        TIMEDPERMISSION_EXPIRED,
        /** The entity's rank within a ladder was changed. */
        RANK_CHANGED,
        /** The default group assignment was changed. */
        DEFAULTGROUP_CHANGED,
        /** The entity's weight (priority) was changed. */
        WEIGHT_CHANGED,
        /** Entity data was persisted to the backend. */
        SAVED,
        /** The entity was deleted from the backend. */
        REMOVED,
    }

    /**
     * Returns the Bukkit handler list for this event type.
     *
     * @return handler list; never {@code null}
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Returns the static Bukkit handler list for {@link PermissionEntityEvent}.
     *
     * @return handler list; never {@code null}
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
