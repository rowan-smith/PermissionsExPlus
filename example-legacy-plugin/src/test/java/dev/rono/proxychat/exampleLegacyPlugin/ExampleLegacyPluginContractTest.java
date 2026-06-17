package dev.rono.proxychat.exampleLegacyPlugin;

import org.bukkit.event.Listener;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import static org.junit.jupiter.api.Assertions.*;

class ExampleLegacyPluginContractTest {

    @Test
    void implementsListenerAndUsesLegacyApi() throws Exception {
        assertTrue(Listener.class.isAssignableFrom(ExampleLegacyPlugin.class));
        assertNotNull(ExampleLegacyPlugin.class.getMethod("onEnable"));
        assertNotNull(ExampleLegacyPlugin.class.getMethod("onJoin", org.bukkit.event.player.PlayerJoinEvent.class));

        assertNotNull(PermissionsEx.class.getMethod("getPermissionManager"));
        assertNotNull(PermissionsEx.class.getMethod("getUser", org.bukkit.entity.Player.class));
        assertNotNull(PermissionManager.class.getMethod("has", java.util.UUID.class, String.class, String.class));
    }
}
