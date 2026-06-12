package ru.tehkode.permissions.events;

import java.util.UUID;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event fired when the permission system itself changes state.
 *
 * <p>Unlike {@link PermissionEntityEvent}, this event describes global operations such as backend
 * switches, full reloads, and debug-mode toggles rather than changes to individual users or groups.</p>
 */
public class PermissionSystemEvent extends PermissionEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    protected Action action;

    /**
     * Creates a system-level permission event.
     *
     * @param sourceUUID UUID of the server that originated this event; must not be {@code null}
     * @param action     kind of system change that occurred; must not be {@code null}
     */
    public PermissionSystemEvent(UUID sourceUUID, Action action) {
        super(sourceUUID);
        this.action = action;
    }

    /**
     * Returns the kind of system change that triggered this event.
     *
     * @return action descriptor; never {@code null}
     */
    public Action getAction() {
        return this.action;
    }

    /**
     * Describes global permission system state changes.
     */
    public enum Action {
        /** The active permission storage backend was changed. */
        BACKEND_CHANGED,
        /** Permission data was reloaded from storage. */
        RELOADED,
        /** World inheritance mappings were changed. */
        WORLDINHERITANCE_CHANGED,
        /** The server-wide default group was changed. */
        DEFAULTGROUP_CHANGED,
        /** Debug logging mode was toggled on or off. */
        DEBUGMODE_TOGGLE,
        /** Permission attachments were re-injected into online players. */
        REINJECT_PERMISSIBLES,
    }

    /**
     * Returns the Bukkit handler list for this event type.
     *
     * @return handler list; never {@code null}
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the static Bukkit handler list for {@link PermissionSystemEvent}.
     *
     * @return handler list; never {@code null}
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
