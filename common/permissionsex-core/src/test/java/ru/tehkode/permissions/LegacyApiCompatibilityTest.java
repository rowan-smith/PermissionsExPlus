package ru.tehkode.permissions;

import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.InternalPermissionManager;
import dev.rono.permissions.core.backends.MultiBackend;
import dev.rono.permissions.core.backends.sql.SQLBackend;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.backends.PermissionBackend;

import static org.junit.jupiter.api.Assertions.*;

public class LegacyApiCompatibilityTest {

    @Test
    public void runtimeManagerExposesInternalHooksSeparatelyFromLegacySurface() throws NoSuchMethodException {
        assertTrue(InternalPermissionManager.class.isAssignableFrom(DefaultPermissionManager.class));
        InternalPermissionManager.class.getMethod("getPlatform");
        InternalPermissionManager.class.getMethod("publishEntity", String.class, String.class,
                dev.rono.permissions.api.bus.EntityMutation.class);
        InternalPermissionManager.class.getMethod("getBasedir");
        InternalPermissionManager.class.getMethod("getWorldNames");
        assertThrows(NoSuchMethodException.class, () ->
                PermissionManager.class.getMethod("getPlatform"));
    }

    @Test
    public void permissionManagerLoadsClassicCompatibilitySurface() throws NoSuchMethodException {
        PermissionManager.class.getMethod("has", java.util.UUID.class, String.class, String.class);
        PermissionManager.class.getMethod("getUser", java.util.UUID.class);
        PermissionManager.class.getMethod("getConfiguration");
        PermissionManager.class.getMethod("shouldSaveDefaultGroup");
        PermissionManager.class.getMethod("reset");
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
