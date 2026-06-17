package dev.rono.permissions.core;

import dev.rono.permissions.api.permission.PermissionContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Server-bound PermissionContext facades (proxy realm names share the world namespace). */
class ModernApiServerContextTest extends ModernApiTestSupport {

    @Test
    void serverContextDelegatesChecksAndMutations() {
        var user = api().getUserManager().createUser("server-ctx-user");
        var ctx = user.inContext(PermissionContext.server("lobby"));

        assertEquals("lobby", ctx.context().get(PermissionContext.SERVER).orElseThrow());
        assertSame(user, ctx.subject());

        ctx.addPermission("lobby.only");
        user.save();

        assertTrue(ctx.has("lobby.only"));
        assertFalse(user.global().has("lobby.only"));
        assertFalse(user.has("lobby.only"));
    }

    @Test
    void serverContextMatchesWorldContextForSameRealm() {
        var user = api().getUserManager().createUser("server-world-parity");
        user.inContext(PermissionContext.server("survival")).addPermission("shared.node");
        user.save();

        assertTrue(user.inContext(PermissionContext.world("survival")).has("shared.node"));
        assertTrue(user.inContext(PermissionContext.server("survival")).has("shared.node"));
    }

    @Test
    void userServerContextGroupOperations() {
        api().getGroupManager().createGroup("server-ctx-group").save();
        var user = api().getUserManager().createUser("server-ctx-group-user");

        user.inContext(PermissionContext.server("lobby")).addGroup("server-ctx-group");
        user.save();

        assertTrue(user.inContext(PermissionContext.server("lobby")).inGroup("server-ctx-group"));
        assertFalse(user.inContext(PermissionContext.server("hub")).inGroup("server-ctx-group"));
    }

    @Test
    void groupServerContextHierarchyOperations() {
        var parent = api().getGroupManager().createGroup("server-parent");
        var child = api().getGroupManager().createGroup("server-child");
        child.inContext(PermissionContext.server("lobby")).addParent(parent.getName());
        child.save();

        assertTrue(child.inContext(PermissionContext.server("lobby")).isChildOf(parent.getName()));
        assertFalse(parent.inContext(PermissionContext.server("lobby")).children().isEmpty());
    }

    @Test
    void emptyServerContextNormalizesToGlobal() {
        var user = api().getUserManager().createUser("server-global-user");
        user.inContext(PermissionContext.server("")).addPermission("server-global-key");
        user.save();

        assertTrue(user.has("server-global-key"));
        assertTrue(PermissionContext.server("").isGlobal());
    }
}
