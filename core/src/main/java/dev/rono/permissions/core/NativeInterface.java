package dev.rono.permissions.core;

import dev.rono.permissions.core.events.PermissionEvent;
import java.util.Collection;
import java.util.UUID;

/**
 * Classic PermissionsEx host SPI (still named {@code NativeInterface} for 1.23.x-era compatibility).
 * Game servers inherit the full definition including {@linkplain #callEvent(PermissionEvent)}; proxy adapters
 * may implement only {@link dev.rono.permissions.api.runtime.PlatformAdapter} instead to avoid dragging in Bukkit classes.
 */
public interface NativeInterface {
    String UUIDToName(UUID uid);

    UUID nameToUUID(String name);

    boolean isOnline(UUID uuid);

    UUID getServerUUID();

    Collection<String> getWorldNames();

    /** Posts a legacy {@linkplain PermissionEvent} on the backing event bus where supported. */
    void callEvent(PermissionEvent event);

    String getOnlineWorldName(UUID uuid);

    String getOnlinePlayerName(UUID uuid);

    boolean isOperator(UUID uuid);
}
