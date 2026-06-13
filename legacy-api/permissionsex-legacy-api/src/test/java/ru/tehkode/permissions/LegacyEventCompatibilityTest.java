package ru.tehkode.permissions;

import org.bukkit.event.Event;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.events.PermissionEvent;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LegacyEventCompatibilityTest {

    @Test
    public void permissionEventIsLegacyBukkitEvent() {
        assertTrue(java.io.Serializable.class.isAssignableFrom(PermissionEvent.class));
        assertTrue(Event.class.isAssignableFrom(PermissionEvent.class));
    }
}
