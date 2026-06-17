package dev.rono.permissions.example;

import dev.rono.permissions.api.permission.PermissionContext;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import static org.junit.jupiter.api.Assertions.*;

class ExamplePluginContractTest {

    @Test
    void implementsListenerAndUsesModernApi() throws Exception {
        assertTrue(Listener.class.isAssignableFrom(ExamplePlugin.class));
        assertNotNull(ExamplePlugin.class.getMethod("onEnable"));
        assertNotNull(ExamplePlugin.class.getMethod("onJoin", org.bukkit.event.player.PlayerJoinEvent.class));
        assertNotNull(PermissionsEx.class.getMethod("getApi"));
        assertNotNull(PermissionsEx.class.getMethod("isAvailable"));
        assertNotNull(PermissionContext.class.getMethod("of", String.class, String.class, String.class, String.class));
    }
}
