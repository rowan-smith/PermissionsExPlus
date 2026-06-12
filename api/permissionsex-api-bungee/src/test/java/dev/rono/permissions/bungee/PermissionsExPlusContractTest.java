package dev.rono.permissions.bungee;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionsExPlusContractTest {

    @Test
    void modernProxyEntryPointsMatchDocumentedSurface() throws NoSuchMethodException {
        assertTrue(Modifier.isFinal(PermissionsExPlus.class.getModifiers()));
        PermissionsExPlus.class.getMethod("getPlugin");
        PermissionsExPlus.class.getMethod("isAvailable");
        PermissionsExPlus.class.getMethod("getPermissionService");
        PermissionsExPlus.class.getMethod("getPermissionManager");
        PermissionsExPlus.class.getMethod("getUser", String.class);
        PermissionsExPlus.class.getMethod("getUser", UUID.class);
    }
}
