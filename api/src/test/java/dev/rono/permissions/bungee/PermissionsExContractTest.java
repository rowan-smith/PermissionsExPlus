package dev.rono.permissions.bungee;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.UUID;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionsExContractTest {

    @Test
    void proxyEntryPointsMatchDocumentedSurface() throws NoSuchMethodException {
        assertTrue(Modifier.isFinal(PermissionsEx.class.getModifiers()));
        PermissionsEx.class.getMethod("getPlugin");
        PermissionsEx.class.getMethod("isAvailable");
        PermissionsEx.class.getMethod("getApi");
        PermissionsEx.class.getMethod("getPermissionManager");
    }
}
