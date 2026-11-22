package dev.rono.permissions.paper.platform;

import dev.rono.permissions.bukkit.platform.PermissionsExPlusPermissible;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rono.permissions.api.permission.PermissionResult;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.permissions.PermissibleBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class PaperPermissibleTest {
    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void permissionChecksAlwaysConsultCoreWithoutWaitingForRecalculation() {
        var player = server.addPlayer("alex");

        // Simulates a timed permission: Core stops returning ALLOW the moment it
        // expires, before any refresh event recalculates the permissible.
        var result = new AtomicReference<>(PermissionResult.ALLOW);

        var permissible = new PermissionsExPlusPermissible(
                player, new PermissibleBase(player), node -> result.get(), Map::of);

        assertTrue(permissible.hasPermission("pex.dynamic"));

        result.set(PermissionResult.DENY);

        assertFalse(permissible.hasPermission("pex.dynamic"), "stale ALLOW served after Core revoked the permission");
    }
}
