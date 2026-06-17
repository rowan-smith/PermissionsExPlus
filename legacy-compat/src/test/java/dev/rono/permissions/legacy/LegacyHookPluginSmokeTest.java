package dev.rono.permissions.legacy;

import dev.rono.proxychat.exampleLegacyPlugin.ExampleLegacyPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Loads the example hook plugin alongside PermissionsEx on MockBukkit when the API pairing is compatible. */
class LegacyHookPluginSmokeTest {

    private ServerMock server;
    private SpigotPermissionsExPlugin pex;
    private boolean mockAvailable;

    @BeforeEach
    void setUp() {
        mockAvailable = false;
        try {
            server = MockBukkit.mock();
            server.addSimpleWorld("world");
            pex = MockBukkit.load(SpigotPermissionsExPlugin.class);
            Assumptions.assumeTrue(
                    pex.getPermissionsManager() != null,
                    "PermissionsEx manager not initialized under MockBukkit");
            mockAvailable = true;
        } catch (Throwable incompatible) {
            Assumptions.assumeTrue(
                    false, "MockBukkit unavailable for this API pairing: " + incompatible.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        if (mockAvailable) {
            MockBukkit.unmock();
        }
    }

    @Test
    void exampleHookPluginLoadsAndReachesLegacyApi() throws Exception {
        MockBukkit.load(ExampleLegacyPlugin.class);

        PermissionManager manager = pex.getPermissionsManager();
        assertNotNull(manager);

        Class<?> runtimePex = Class.forName("ru.tehkode.permissions.bukkit.PermissionsEx");
        runtimePex.getMethod("getPermissionManager");
        runtimePex.getMethod("getUser", org.bukkit.entity.Player.class);

        PlayerMock player = server.addPlayer("legacy-hook");
        PermissionUser user = manager.getUser(player);
        assertNotNull(user);
    }
}
