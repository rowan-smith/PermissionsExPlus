package ru.tehkode.permissions;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.core.DefaultPermissionManager;
import org.junit.jupiter.api.Test;
import dev.rono.permissions.core.backends.MultiBackend;
import ru.tehkode.permissions.backends.PermissionBackend;
import dev.rono.permissions.core.backends.sql.SQLBackend;
import dev.rono.permissions.core.events.PermissionEvent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LegacyApiCompatibilityTest {

    @Test
    public void permissionEventIsLegacyBukkitEvent() {
        assertTrue(java.io.Serializable.class.isAssignableFrom(PermissionEvent.class));
        assertTrue(org.bukkit.event.Event.class.isAssignableFrom(PermissionEvent.class));
    }

    @Test
    public void permissionManagerImplementsModernServiceToken() {
        assertTrue(PermissionService.class.isAssignableFrom(DefaultPermissionManager.class));
    }

    @Test
    public void permissionManagerLoadsOneTwoThreeCompatibilitySurface() throws NoSuchMethodException {
        PermissionManager.class.getMethod("has", java.util.UUID.class, String.class, String.class);
        PermissionManager.class.getMethod("getUser", java.util.UUID.class);
        PermissionManager.class.getMethod("resetUser", String.class);
        PermissionManager.class.getMethod("clearUserCache", java.util.UUID.class);
        PermissionManager.class.getMethod("getDefaultGroup");
        PermissionManager.class.getMethod("getDefaultGroup", String.class);
        PermissionManager.class.getMethod("reload");
        PermissionManager.class.getMethod("createUser", String.class);
        PermissionManager.class.getMethod("createUser", java.util.UUID.class);
        PermissionManager.class.getMethod("removeUser", String.class);
        PermissionManager.class.getMethod("removeUser", java.util.UUID.class);
        PermissionManager.class.getMethod("removeGroup", String.class);
    }

    @Test
    public void backendsExposeCoreImplementationsOnClasspath() throws NoSuchMethodException {
        assertNotNull(PermissionBackend.class.getMethod("getBackend",
                String.class, PermissionManager.class, PEXBackendConfiguration.class));
        assertTrue(PermissionBackend.class.isAssignableFrom(SQLBackend.class));
        assertTrue(PermissionBackend.class.isAssignableFrom(MultiBackend.class));
    }

    @Test
    public void permissionUserDroppedBukkitPlayersFromCoreType() {
        assertThrows(NoSuchMethodException.class, () -> PermissionUser.class.getMethod("getPlayer"));
    }
}
