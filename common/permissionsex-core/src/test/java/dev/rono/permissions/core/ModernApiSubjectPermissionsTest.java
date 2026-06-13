package dev.rono.permissions.core;

import dev.rono.permissions.api.world.Worlds;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Permission, option, prefix/suffix, and inheritance behavior on modern subjects. */
class ModernApiSubjectPermissionsTest extends ModernApiTestSupport {

    @Test
    void directAndEffectivePermissions() {
        var group = api().getGroupManager().createGroup("perm-parent");
        group.addPermission("parent.node", Worlds.GLOBAL);
        group.save();

        var user = api().getUserManager().createUser("perm-user");
        user.addPermission("user.node", Worlds.GLOBAL);
        user.addGroup(group.getName(), Worlds.GLOBAL);
        user.save();

        assertTrue(user.permissions().contains("user.node"));
        assertTrue(user.effectivePermissions().contains("user.node"));
        assertTrue(user.effectivePermissions().contains("parent.node"));
        assertFalse(user.permissions().contains("parent.node"));
    }

    @Test
    void negatedPermissionBlocksGrant() {
        var user = api().getUserManager().createUser("neg-user");
        user.addPermission("allowed.node", Worlds.GLOBAL);
        user.addPermission("-allowed.node", Worlds.GLOBAL);
        user.save();

        assertFalse(user.hasPermission("allowed.node"));
    }

    @Test
    void optionsPrefixSuffix() {
        var user = api().getUserManager().createUser("meta-user");
        user.setOption("custom", "value", Worlds.GLOBAL);
        user.setPrefix("[P]", Worlds.GLOBAL);
        user.setSuffix("[S]", Worlds.GLOBAL);
        user.save();

        assertEquals("value", user.option("custom"));
        assertEquals("[P]", user.prefix());
        assertEquals("[S]", user.suffix());
        assertTrue(user.options().containsKey("custom"));
    }

    @Test
    void setPermissionsReplacesDirectAssignments() {
        var user = api().getUserManager().createUser("set-perms-user");
        user.addPermission("a", Worlds.GLOBAL);
        user.addPermission("b", Worlds.GLOBAL);
        user.setPermissions(List.of("c"), Worlds.GLOBAL);
        user.save();

        assertEquals(List.of("c"), user.permissions());
        assertTrue(user.hasPermission("c"));
        assertFalse(user.hasPermission("a"));
    }

    @Test
    void timedPermissionMetadata() {
        var user = api().getUserManager().createUser("timed-perm-user");
        user.addTimedPermission("temp.node", Worlds.GLOBAL, 300);
        user.save();

        assertTrue(user.hasTimedPermission("temp.node"));
        assertTrue(user.timedPermissionRemainingSeconds("temp.node") > 0);
        assertEquals(1, user.timedPermissionEntries().size());
        assertEquals("temp.node", user.timedPermissionEntries().get(0).permission());
    }

    @Test
    void permissionsByWorldMapsAreSnapshots() {
        var user = api().getUserManager().createUser("map-user");
        user.inWorld("arena").addPermission("arena.node");
        user.save();

        var map = user.permissionsByWorld();
        assertTrue(map.containsKey("arena"));
        assertThrows(UnsupportedOperationException.class, () -> map.put("x", List.of()));
        assertTrue(user.permissionsByWorld().containsKey("arena"));
    }

    @Test
    void subjectIdentityFields() {
        var id = java.util.UUID.randomUUID();
        var user = api().getUserManager().createUser(id);
        user.setOption("name", "identity-name", Worlds.GLOBAL);
        user.save();

        assertEquals(id, user.getId());
        assertEquals(id, user.uniqueId().orElseThrow());
        assertEquals("identity-name", user.name());
        assertEquals(id.toString(), user.identifier());
        assertFalse(user.virtual());
        assertEquals(dev.rono.permissions.api.permission.HolderType.USER, user.asHolder().getType());
    }
}
