package ru.tehkode.permissions;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupMembershipIndexTest extends PEXTestBase {

    @Test
    public void getUsersUsesIndexForDirectMembership() {
        PermissionUser alice = manager.getUser("alice");
        PermissionUser bob = manager.getUser("bob");
        PermissionGroup mod = manager.getGroup("moderator");

        alice.addGroup(mod);
        bob.addGroup(manager.getGroup("default"));

        Set<PermissionUser> mods = manager.getUsers("moderator");
        assertEquals(1, mods.size());
        assertTrue(mods.contains(alice));
    }

    @Test
    public void getUsersWithInheritanceIncludesChildGroups() {
        PermissionGroup parent = manager.getGroup("parent");
        PermissionGroup child = manager.getGroup("child");
        child.setParents(Collections.singletonList(parent));

        PermissionUser user = manager.getUser("member");
        user.addGroup(child);

        Set<PermissionUser> parentMembers = manager.getUsers("parent", null, true);
        assertTrue(parentMembers.contains(user));
    }
}
