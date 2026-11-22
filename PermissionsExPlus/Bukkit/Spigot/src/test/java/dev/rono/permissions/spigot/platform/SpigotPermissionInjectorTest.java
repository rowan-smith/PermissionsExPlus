package dev.rono.permissions.spigot.platform;

import dev.rono.permissions.bukkit.platform.BukkitPermissionInjector;
import dev.rono.permissions.bukkit.platform.PermissionsExPlusPermissible;
import dev.rono.permissions.api.permission.PermissionResult;
import org.bukkit.permissions.PermissibleBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpigotPermissionInjectorTest {
    private ServerMock server;

    private PluginMock plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();

        plugin = MockBukkit.createMockPlugin();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void unsupportedPlayerImplementationDisablesOnlyTheOptionalInjector() {
        var injector = new BukkitPermissionInjector(plugin,
                (player, node) -> PermissionResult.UNDEFINED);

        var player = server.addPlayer("alex");

        assertFalse(injector.injectOrRefuse(player));
        server.getScheduler().performOneTick();

        assertTrue(player.isOnline(), "players stay unless injection is required");
        assertEquals(BukkitPermissionInjector.PermissionHookStatus.UNAVAILABLE, injector.status());
        assertTrue(injector.healthLine().contains("NOT controlled"));
        assertTrue(injector.failureReason().isPresent());

        injector.close();
    }

    @Test
    void requiredInjectionRefusesPlayersItCannotControl() {
        var injector = new BukkitPermissionInjector(plugin,
                (player, node) -> PermissionResult.UNDEFINED);
        injector.requireInjection(true);

        var player = server.addPlayer("alex");

        assertFalse(injector.injectOrRefuse(player));
        server.getScheduler().performOneTick();

        assertFalse(player.isOnline(), "fail closed: the player must not play with uncontrolled permissions");
        assertTrue(injector.healthLine().contains("players are refused"));

        injector.close();
    }

    @Test
    void permissibleCombinesPexResolutionWithBukkitAttachments() {
        var player = server.addPlayer("alex");

        var delegate = new PermissibleBase(player);

        delegate.addAttachment(plugin, "bukkit.attachment", true);

        var permissible = new PermissionsExPlusPermissible(player, delegate, node -> switch (node) {
            case "pex.allowed" -> PermissionResult.ALLOW;

            case "pex.denied" -> PermissionResult.DENY;

            default -> PermissionResult.UNDEFINED;
        }, () -> java.util.Map.of("pex.wildcard.*", PermissionResult.ALLOW));

        assertTrue(permissible.hasPermission("pex.allowed"));

        assertFalse(permissible.hasPermission("pex.denied"));

        assertTrue(permissible.hasPermission("bukkit.attachment"));

        assertTrue(permissible.isPermissionSet("pex.allowed"));

        assertTrue(permissible.getEffectivePermissions().stream()
                .anyMatch(info -> info.getPermission().equals("pex.allowed") && info.getValue()));

        assertTrue(permissible.getEffectivePermissions().stream()
                .anyMatch(info -> info.getPermission().equals("pex.wildcard.*") && info.getValue()));
    }

    @Test
    void permissionChecksAlwaysConsultCoreWithoutWaitingForRecalculation() {
        var player = server.addPlayer("alex");

        // Simulates a timed permission: Core stops returning ALLOW the moment it
        // expires, before any refresh event recalculates the permissible.
        var result = new AtomicReference<>(PermissionResult.ALLOW);

        var permissible = new PermissionsExPlusPermissible(
                player, new PermissibleBase(player), node -> result.get(), java.util.Map::of);

        assertTrue(permissible.hasPermission("pex.dynamic"));
        assertTrue(permissible.isPermissionSet("pex.dynamic"));

        result.set(PermissionResult.DENY);

        assertFalse(permissible.hasPermission("pex.dynamic"), "stale ALLOW served after Core revoked the permission");

        result.set(PermissionResult.UNDEFINED);

        assertFalse(permissible.isPermissionSet("pex.dynamic"));
    }
}
