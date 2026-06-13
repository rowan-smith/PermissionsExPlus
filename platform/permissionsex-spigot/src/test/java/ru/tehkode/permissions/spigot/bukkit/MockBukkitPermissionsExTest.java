package ru.tehkode.permissions.spigot.bukkit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.spigot.bukkit.regexperms.PermissiblePEX;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Full-server integration tests via MockBukkit. Skipped automatically when the installed
 * MockBukkit build is incompatible with the compile-time Bukkit API on the classpath.
 */
class MockBukkitPermissionsExTest {

    private ServerMock server;
    private SpigotPermissionsExPlugin plugin;
    private boolean mockAvailable;

    @BeforeEach
    void setUp() {
        mockAvailable = false;
        try {
            server = MockBukkit.mock();
            server.addSimpleWorld("world");
            plugin = MockBukkit.load(SpigotPermissionsExPlugin.class);
            mockAvailable = true;
        } catch (Throwable incompatible) {
            Assumptions.assumeTrue(false,
                    "MockBukkit unavailable for this API pairing: " + incompatible.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        if (mockAvailable) {
            MockBukkit.unmock();
        }
    }

    @Test
    void pluginEnablesAndRegistersManager() {
        assertNotNull(plugin.getPermissionsManager());
    }

    @Test
    void managerGrantVisibleThroughHas() {
        PlayerMock player = server.addPlayer("Steve");
        PermissionUser user = plugin.getPermissionsManager().getUser(player.getUniqueId());
        user.addPermission("mockbukkit.test", null);
        assertTrue(plugin.getPermissionsManager().has(player.getUniqueId(), "mockbukkit.test", "world"));
    }

    @Test
    void injectsPermissibleAndSupportsCacheClear() {
        PlayerMock player = server.addPlayer("CacheTest");
        plugin.getRegexPerms().injectPermissible(player);

        PermissiblePEX permissible = plugin.getRegexPerms().getInjectedPermissible(player);
        assertNotNull(permissible);
        permissible.clearPermissionCache();
    }
}
