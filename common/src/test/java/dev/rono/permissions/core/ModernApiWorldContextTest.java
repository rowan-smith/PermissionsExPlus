package dev.rono.permissions.core;

import dev.rono.permissions.api.permission.PermissionContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** PermissionContext-bound facades and global vs per-realm scoping. */
class ModernApiWorldContextTest extends ModernApiTestSupport {

    @Test
    void inContextDelegatesChecksAndMutations() {
        var user = api().getUserManager().createUser("ctx-user");
        var ctx = user.inContext(PermissionContext.world("survival"));

        assertEquals("survival", ctx.context().get(PermissionContext.WORLD).orElseThrow());
        assertSame(user, ctx.subject());

        ctx.addPermission("survival.only");
        user.save();

        assertTrue(ctx.has("survival.only"));
        assertFalse(user.global().has("survival.only"));
        assertFalse(user.has("survival.only"));
    }

    @Test
    void globalContextUsesGlobalNamespace() {
        var user = api().getUserManager().createUser("global-ctx-user");
        user.global().addPermission("global.node");
        user.save();

        assertTrue(user.has("global.node"));
        assertTrue(user.global().has("global.node"));
        assertTrue(user.global().context().isGlobal());
    }

    @Test
    void userContextGroupOperations() {
        api().getGroupManager().createGroup("ctx-group").save();
        var user = api().getUserManager().createUser("ctx-group-user");

        user.inContext(PermissionContext.world("w")).addGroup("ctx-group");
        user.save();

        assertTrue(user.inContext(PermissionContext.world("w")).inGroup("ctx-group"));
        assertFalse(user.inContext(PermissionContext.world("other")).inGroup("ctx-group"));
    }

    @Test
    void groupContextHierarchyOperations() {
        var parent = api().getGroupManager().createGroup("ctx-parent");
        var child = api().getGroupManager().createGroup("ctx-child");
        child.inContext(PermissionContext.world("w")).addParent(parent.getName());
        child.save();

        assertTrue(child.inContext(PermissionContext.world("w")).isChildOf(parent.getName()));
        assertFalse(parent.inContext(PermissionContext.world("w")).children().isEmpty());
    }

    @Test
    void emptyWorldContextNormalizesToGlobal() {
        var user = api().getUserManager().createUser("normalize-user");
        user.inContext(PermissionContext.world("")).addPermission("empty-world-key");
        user.save();

        assertTrue(user.has("empty-world-key"));
        assertTrue(PermissionContext.world("").isGlobal());
    }
}
