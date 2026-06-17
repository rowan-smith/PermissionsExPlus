package dev.rono.permissions.bungee;

import dev.rono.permissions.api.PermissionsExApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PermissionManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProxyPermissionServicesTest {

    @AfterEach
    void tearDown() {
        ProxyPermissionServices.unregister();
    }

    @Test
    void modernRegistrationDoesNotActivateLegacy() {
        var api = mock(PermissionsExApi.class);
        ProxyPermissionServices.registerModern(api);

        assertTrue(ProxyPermissionServices.isRegistered());
        assertFalse(ProxyPermissionServices.isLegacyActive());
        assertSame(api, ProxyPermissionServices.permissionsExApi());
        assertThrows(IllegalStateException.class, ProxyPermissionServices::permissionManager);
    }

    @Test
    void legacyActivationRegistersPermissionManager() {
        var api = mock(PermissionsExApi.class);
        var manager = mock(PermissionManager.class);
        when(api.getPermissionManager()).thenReturn(manager);

        ProxyPermissionServices.registerModern(api);
        ProxyPermissionServices.activateLegacy(manager);

        assertTrue(ProxyPermissionServices.isLegacyActive());
        assertSame(manager, ProxyPermissionServices.permissionManager());
    }

    @Test
    void registerRegistersBothSurfaces() {
        var api = mock(PermissionsExApi.class);
        var manager = mock(PermissionManager.class);

        ProxyPermissionServices.register(api, manager);

        assertTrue(ProxyPermissionServices.isRegistered());
        assertTrue(ProxyPermissionServices.isLegacyActive());
    }
}
