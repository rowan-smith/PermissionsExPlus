package ru.tehkode.permissions;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PermissionEntityTest extends PEXTestBase {

    @Test
    public void testAddTimedPermission() {
        PermissionUser user = manager.getUser("TestUser");
        user.addTimedPermission("test.timed.perm", null, 300);
        assertTrue(user.has("test.timed.perm"));
        List<String> timedPerms = user.getTimedPermissions(null);
        assertTrue(timedPerms.contains("test.timed.perm"));
    }

    @Test
    public void testTimedPermissionNotInOwnPermissions() {
        PermissionUser user = manager.getUser("TestUser");
        user.addPermission("test.regular.perm");
        user.addTimedPermission("test.timed.perm", null, 300);
        List<String> ownPerms = user.getOwnPermissions(null);
        assertTrue(ownPerms.contains("test.regular.perm"));
        assertFalse(ownPerms.contains("test.timed.perm"));
    }

    @Test
    public void testTimedPermissionGetsOwnPermissionsIsCorrect() {
        PermissionUser user = manager.getUser("TestUser");
        user.addTimedPermission("test.timed.perm", null, 300);
        List<String> perms = user.getOwnPermissions(null);
        for (String perm : perms) {
            assertFalse(perm.contains(":"), "Timed serialized entry leaked into own permissions: " + perm);
        }
    }

    @Test
    public void testTimedPermissionPersistsAcrossReload() {
        PermissionUser user = manager.getUser("TestUser");
        user.addTimedPermission("test.timed.perm", null, 300);
        manager.resetUser("TestUser");
        PermissionUser reloaded = manager.getUser("TestUser");
        reloaded.initialize();
        assertTrue(reloaded.has("test.timed.perm"));
        List<String> timedPerms = reloaded.getTimedPermissions(null);
        assertTrue(timedPerms.contains("test.timed.perm"));
    }

    @Test
    public void testTimedPermissionPersistsAcrossReloadMultipleWorlds() {
        PermissionUser user = manager.getUser("TestUser");
        user.addTimedPermission("test.timed.perm", null, 300);
        user.addTimedPermission("test.world.perm", "world", 300);
        manager.resetUser("TestUser");
        PermissionUser reloaded = manager.getUser("TestUser");
        reloaded.initialize();
        assertTrue(reloaded.has("test.timed.perm"));
        assertTrue(reloaded.has("test.world.perm", "world"));
    }

    @Test
    public void testRemoveTimedPermission() {
        PermissionUser user = manager.getUser("TestUser");
        user.addTimedPermission("test.timed.perm", null, 300);
        assertTrue(user.has("test.timed.perm"));
        user.removeTimedPermission("test.timed.perm", null);
        assertFalse(user.has("test.timed.perm"));
        manager.resetUser("TestUser");
        PermissionUser reloaded = manager.getUser("TestUser");
        reloaded.initialize();
        assertFalse(reloaded.has("test.timed.perm"));
    }

    @Test
    public void testSetPermissionsPreservesTimedPermissions() {
        PermissionUser user = manager.getUser("TestUser");
        user.addTimedPermission("test.timed.perm", null, 300);
        user.setPermissions(List.of("new.perm"), null);
        assertTrue(user.has("test.timed.perm"), "Timed permission should survive setPermissions");
        assertTrue(user.has("new.perm"), "New permission should be present");
    }

    @Test
    public void testTransientTimedPermissionNotPersisted() {
        PermissionUser user = manager.getUser("TestUser");
        user.addTimedPermission("test.transient.perm", null, 0);
        assertTrue(user.has("test.transient.perm"));
        manager.resetUser("TestUser");
        PermissionUser reloaded = manager.getUser("TestUser");
        reloaded.initialize();
        assertFalse(reloaded.has("test.transient.perm"), "Transient (lifeTime=0) permissions should not persist");
    }

    @Test
    public void testTimedPermissionOnGroup() {
        PermissionGroup group = manager.getGroup("TestGroup");
        group.addTimedPermission("test.group.timed", null, 300);
        assertTrue(group.has("test.group.timed"));
        List<String> timedPerms = group.getTimedPermissions(null);
        assertTrue(timedPerms.contains("test.group.timed"));
    }

    @Test
    public void testGroupTimedPermissionPersistsAcrossReload() {
        PermissionGroup group = manager.getGroup("TestGroup");
        group.addTimedPermission("test.group.timed", null, 300);
        PermissionGroup reloaded = manager.getGroup("TestGroup");
        reloaded.initialize();
        assertTrue(reloaded.has("test.group.timed"));
    }

    @Test
    public void testGetTimedPermissionLifetime() {
        PermissionUser user = manager.getUser("TestUser");
        user.addTimedPermission("test.timed.perm", null, 60);
        int lifetime = user.getTimedPermissionLifetime("test.timed.perm", null);
        assertTrue(lifetime > 0 && lifetime <= 60, "Lifetime should be positive and <= 60");
    }

    @Test
    public void testOwnPermissionsDoesNotContainSerializedEntries() {
        PermissionUser user = manager.getUser("TestUser");
        user.addTimedPermission("test.timed.perm", null, 300);
        user.addTimedPermission("another.perm", null, 300);
        user.addPermission("regular.perm");
        List<String> ownPerms = user.getOwnPermissions(null);
        assertEquals(1, ownPerms.size(), "Only regular permission should appear in own permissions");
        assertTrue(ownPerms.contains("regular.perm"));
    }
}