package dev.rono.permissions.core;

import dev.rono.permissions.api.world.Worlds;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** World-bound context facades and global vs per-world scoping. */
class ModernApiWorldContextTest extends ModernApiTestSupport {

    @Test
    void inWorldDelegatesChecksAndMutations() {
        var user = api().getUserManager().createUser("ctx-user");
        var ctx = user.inWorld("survival");

        assertEquals("survival", ctx.world());
        assertSame(user, ctx.subject());

        ctx.addPermission("survival.only");
        user.save();

        assertTrue(ctx.hasPermission("survival.only"));
        assertFalse(user.global().hasPermission("survival.only"));
        assertFalse(user.hasPermission("survival.only"));
    }

    @Test
    void globalContextUsesGlobalNamespace() {
        var user = api().getUserManager().createUser("global-ctx-user");
        user.global().addPermission("global.node");
        user.save();

        assertTrue(user.hasPermission("global.node"));
        assertTrue(user.global().hasPermission("global.node"));
        assertNull(user.global().world());
    }

    @Test
    void userWorldContextGroupOperations() {
        api().getGroupManager().createGroup("ctx-group").save();
        var user = api().getUserManager().createUser("ctx-group-user");

        user.inWorld("w").addGroup("ctx-group");
        user.save();

        assertTrue(user.inWorld("w").inGroup("ctx-group"));
        assertFalse(user.inWorld("other").inGroup("ctx-group"));
    }

    @Test
    void groupWorldContextHierarchyOperations() {
        var parent = api().getGroupManager().createGroup("ctx-parent");
        var child = api().getGroupManager().createGroup("ctx-child");
        child.inWorld("w").addParent(parent.getName());
        child.save();

        assertTrue(child.inWorld("w").isChildOf(parent.getName()));
        assertFalse(parent.inWorld("w").children().isEmpty());
    }

    @Test
    void worldsGlobalNormalizesEmptyString() {
        var user = api().getUserManager().createUser("normalize-user");
        user.inWorld("").addPermission("empty-world-key");
        user.save();

        assertTrue(user.hasPermission("empty-world-key"));
        assertEquals(Worlds.GLOBAL, Worlds.normalize(""));
        assertTrue(Worlds.isGlobal(null));
    }
}
