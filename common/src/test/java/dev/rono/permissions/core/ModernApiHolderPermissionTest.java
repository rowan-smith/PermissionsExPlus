package dev.rono.permissions.core;

import dev.rono.permissions.api.permission.PermissionAddRequest;
import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.permission.PermissionSource;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Holder-based permission checks and PermissionAddRequest grants. */
class ModernApiHolderPermissionTest extends ModernApiTestSupport {

    @Test
    void holderGlobalGrantAndCheck() {
        var user = api().getUserManager().createUser(UUID.randomUUID());
        var node = api().getPermissionManager().addPermission(user.asHolder(), "holder.global");
        user.save();

        assertNotNull(node);
        assertEquals("holder.global", node.permission());
        assertTrue(api().getPermissionManager().hasPermission(user.asHolder(), "holder.global"));
    }

    @Test
    void holderWorldContextCheck() {
        var user = api().getUserManager().createUser("holder-ctx-user");
        api().getPermissionManager().addPermission(
                PermissionAddRequest.builder()
                        .holder(user.asHolder())
                        .permission("holder.world.node")
                        .addContext(PermissionContext.WORLD, "ctx-realm")
                        .source(PermissionSource.SYSTEM)
                        .build());
        user.save();

        assertTrue(api().getPermissionManager().hasPermission(
                user.asHolder(), "holder.world.node", Map.of("world", "ctx-realm")));
        assertFalse(api().getPermissionManager().hasPermission(
                user.asHolder(), "holder.world.node", Map.of("world", "other-realm")));
    }

    @Test
    void holderTimedGrantViaDuration() {
        var user = api().getUserManager().createUser(UUID.randomUUID());
        var node = api().getPermissionManager().addPermission(
                user.asHolder(), "holder.timed", Duration.ofMinutes(5));
        user.save();

        assertNotNull(node.expiresAt());
        assertTrue(user.has("holder.timed"));
    }

    @Test
    void holderRemovePermission() {
        var user = api().getUserManager().createUser(UUID.randomUUID());
        api().getPermissionManager().addPermission(user.asHolder(), "holder.remove");
        user.save();
        assertTrue(api().getPermissionManager().hasPermission(user.asHolder(), "holder.remove"));

        api().getPermissionManager().removePermission(user.asHolder(), "holder.remove");
        user.save();
        assertFalse(api().getPermissionManager().hasPermission(user.asHolder(), "holder.remove"));
    }

    @Test
    void getPermissionsListsDirectGlobalGrants() {
        var user = api().getUserManager().createUser(UUID.randomUUID());
        api().getPermissionManager().addPermission(user.asHolder(), "listed.node");
        user.save();

        var nodes = api().getPermissionManager().getPermissions(user.asHolder());
        assertTrue(nodes.stream().anyMatch(n -> "listed.node".equals(n.permission())));
    }
}
