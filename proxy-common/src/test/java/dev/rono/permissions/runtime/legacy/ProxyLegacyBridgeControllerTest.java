package dev.rono.permissions.runtime.legacy;

import dev.rono.permissions.bungee.ProxyPermissionServices;
import dev.rono.permissions.core.DefaultPermissionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProxyLegacyBridgeControllerTest {

    @AfterEach
    void tearDown() {
        ProxyPermissionServices.unregister();
    }

    @Test
    void activatesLegacyRegistrationOnce() {
        var manager = mock(DefaultPermissionManager.class);
        var api = mock(dev.rono.permissions.api.PermissionsExApi.class);
        when(manager.permissionsExApi()).thenReturn(api);

        var controller = new ProxyLegacyBridgeController(manager);
        assertFalse(controller.isActive());

        controller.activate("test", Logger.getLogger("test"));
        assertTrue(controller.isActive());
        assertTrue(ProxyPermissionServices.isLegacyActive());

        controller.activate("again", Logger.getLogger("test"));
        assertTrue(controller.isActive());
    }
}
