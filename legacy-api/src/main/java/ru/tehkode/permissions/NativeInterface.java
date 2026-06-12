package ru.tehkode.permissions;

import java.util.UUID;
import ru.tehkode.permissions.events.PermissionEvent;

/**
 * Classic PermissionsEx host SPI ({@code 1.23.x}-era surface). Proxy hosts may implement only
 * {@link dev.rono.permissions.api.runtime.PlatformAdapter} for platform-neutral integration.
 */
public interface NativeInterface {
    String UUIDToName(UUID uid);

    UUID nameToUUID(String name);

    boolean isOnline(UUID uuid);

    UUID getServerUUID();

    void callEvent(PermissionEvent event);
}
