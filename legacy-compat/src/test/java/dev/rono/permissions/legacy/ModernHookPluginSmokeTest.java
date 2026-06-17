package dev.rono.permissions.legacy;

import dev.rono.permissions.example.ExamplePlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Loads the modern example hook plugin alongside PermissionsEx on MockBukkit when compatible. */
class ModernHookPluginSmokeTest {

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
    void exampleHookPluginLoadsAndReachesModernApi() throws Exception {
        MockBukkit.load(ExamplePlugin.class);

        Class<?> runtimePex = Class.forName("ru.tehkode.permissions.bukkit.PermissionsEx");
        assertNotNull(runtimePex.getMethod("getApi"));
        assertNotNull(runtimePex.getMethod("isAvailable"));

        PlayerMock player = server.addPlayer("modern-hook");
        assertNotNull(pex.getPermissionsManager().getUser(player));
    }
}
