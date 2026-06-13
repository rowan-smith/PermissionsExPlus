package dev.rono.permissions.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Permission, option, prefix/suffix, and inheritance behavior on modern subjects. */
class ModernApiSubjectPermissionsTest extends ModernApiTestSupport {

    @Test
    void directAndEffectivePermissions() {
        var group = api().getGroupManager().createGroup("perm-parent");
        group.addPermission("parent.node");
        group.save();

        var user = api().getUserManager().createUser("perm-user");
        user.addPermission("user.node");
        user.addGroup(group.getName());
        user.save();

        assertTrue(user.permissions().contains("user.node"));
        assertTrue(user.effectivePermissions().contains("user.node"));
        assertTrue(user.effectivePermissions().contains("parent.node"));
        assertFalse(user.permissions().contains("parent.node"));
    }

    @Test
    void negatedPermissionBlocksGrant() {
        var user = api().getUserManager().createUser("neg-user");
        user.addPermission("allowed.node");
        user.addPermission("-allowed.node");
        user.save();

        assertFalse(user.has("allowed.node"));
    }

    @Test
    void optionsPrefixSuffix() {
        var user = api().getUserManager().createUser("meta-user");
        user.setOption("custom", "value");
        user.setPrefix("[P]");
        user.setSuffix("[S]");
        user.save();

        assertEquals("value", user.option("custom"));
        assertEquals("[P]", user.prefix());
        assertEquals("[S]", user.suffix());
        assertTrue(user.options().containsKey("custom"));
    }

    @Test
    void setPermissionsReplacesDirectAssignments() {
        var user = api().getUserManager().createUser("set-perms-user");
        user.addPermission("a");
        user.addPermission("b");
        user.setPermissions(List.of("c"));
        user.save();

        assertEquals(List.of("c"), user.permissions());
        assertTrue(user.has("c"));
        assertFalse(user.has("a"));
    }

    @Test
    void timedPermissionMetadata() {
        var user = api().getUserManager().createUser("timed-perm-user");
        user.addTimedPermission("temp.node", 300);
        user.save();

        assertTrue(user.hasTimedPermission("temp.node"));
        assertTrue(user.timedPermissionRemainingSeconds("temp.node") > 0);
        assertEquals(1, user.timedPermissionEntries().size());
        assertEquals("temp.node", user.timedPermissionEntries().get(0).permission());
    }

    @Test
    void permissionsByRealmMapsAreSnapshots() {
        var user = api().getUserManager().createUser("map-user");
        user.inContext(dev.rono.permissions.api.permission.PermissionContext.world("arena")).addPermission("arena.node");
        user.save();

        var map = user.permissionsByRealm();
        assertTrue(map.containsKey("arena"));
        assertThrows(UnsupportedOperationException.class, () -> map.put("x", List.of()));
        assertTrue(user.permissionsByRealm().containsKey("arena"));
    }

    @Test
    void subjectIdentityFields() {
        var id = java.util.UUID.randomUUID();
        var user = api().getUserManager().createUser(id);
        user.setOption("name", "identity-name");
        user.save();

        assertEquals(id, user.getId());
        assertEquals(id, user.uniqueId().orElseThrow());
        assertEquals("identity-name", user.name());
        assertEquals(id.toString(), user.identifier());
        assertFalse(user.virtual());
        assertEquals(dev.rono.permissions.api.permission.HolderType.USER, user.asHolder().getType());
    }
}
