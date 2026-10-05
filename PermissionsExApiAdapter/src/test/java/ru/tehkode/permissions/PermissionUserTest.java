package ru.tehkode.permissions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class PermissionUserTest extends PEXTestBase {

    @Test
    public void testBasicPermissions() {
        PermissionUser user = manager.getUser("TestUser");
        user.addPermission("test.permission");

        assertTrue(user.has("test.permission"), "User should have test.permission");
        assertFalse(user.has("other.permission"), "User should not have other.permission");
    }

    @Test
    public void testWorldSpecificPermissions() {
        PermissionUser user = manager.getUser("TestUser");
        user.addPermission("test.permission", "world1");

        assertTrue(user.has("test.permission", "world1"), "User should have test.permission in world1");
        assertFalse(user.has("test.permission", "world2"), "User should not have test.permission in world2");
        assertFalse(user.has("test.permission"), "User should not have test.permission globally");
    }

    @Test
    public void testGroupInheritance() {
        PermissionUser user = manager.getUser("TestUser");
        PermissionGroup group = manager.getGroup("TestGroup");

        group.addPermission("group.permission");
        user.addGroup(group);

        assertTrue(user.has("group.permission"), "User should inherit group.permission from TestGroup");
    }

    @Test
    public void testPrefixSuffix() {
        PermissionUser user = manager.getUser("TestUser");
        user.setPrefix("[Prefix]", null);
        user.setSuffix("[Suffix]", null);

        assertEquals("[Prefix]", user.getPrefix(null));
        assertEquals("[Suffix]", user.getSuffix(null));
    }

    @Test
    public void testRemoveGroupClearsInheritance() {
        PermissionUser user = manager.getUser("Member");
        PermissionGroup group = manager.getGroup("MemberGroup");

        group.addPermission("member.only");
        user.addGroup(group);
        assertTrue(user.has("member.only"));

        user.removeGroup(group);
        assertFalse(user.has("member.only"));
    }

    @Test
    public void testOwnPermissionsMapExcludesInherited() {
        PermissionUser user = manager.getUser("OwnMap");
        PermissionGroup group = manager.getGroup("Inherited");

        group.addPermission("inherited.perm");
        user.addGroup(group);
        user.addPermission("own.perm");

        assertTrue(user.getOwnPermissions(null).contains("own.perm"));
        assertFalse(user.getOwnPermissions(null).contains("inherited.perm"));
        assertTrue(user.has("inherited.perm"));
    }
}
