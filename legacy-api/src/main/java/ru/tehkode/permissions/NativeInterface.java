package ru.tehkode.permissions;

import ru.tehkode.permissions.events.PermissionEvent;

import java.util.UUID;

/**
 * Classic PermissionsEx host SPI ({@code PermissionsEx 1.23.4}-era surface).
 *
 * <p>Platform integrations implement this interface to bridge player identity, online status, and
 * event dispatch to the permission engine. Proxy hosts may implement only
 * platform {@code PlatformAdapter} for platform-neutral integration.</p>
 */
public interface NativeInterface {
    /**
     * Resolves a player UUID to their current display name.
     *
     * @param uid player UUID
     * @return player name, or {@code null} if the UUID is unknown
     */
    String UUIDToName(UUID uid);

    /**
     * Resolves a player name to their UUID.
     *
     * @param name player name (case sensitivity depends on the platform)
     * @return player UUID, or {@code null} if the name is unknown or offline-only
     */
    UUID nameToUUID(String name);

    /**
     * Returns whether the player identified by {@code uuid} is currently online on this server.
     *
     * @param uuid player UUID
     * @return {@code true} if the player is connected
     */
    boolean isOnline(UUID uuid);

    /**
     * Returns the UUID that identifies this server instance in network events.
     *
     * @return server UUID; never {@code null}
     */
    UUID getServerUUID();

    /**
     * Dispatches a {@link PermissionEvent} through the platform event bus.
     *
     * @param event permission event to fire; must not be {@code null}
     */
    void callEvent(PermissionEvent event);
}
