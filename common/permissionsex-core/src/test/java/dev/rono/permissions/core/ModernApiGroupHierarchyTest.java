package dev.rono.permissions.core;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.user.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Group membership graph, parent trees, and inherit flag semantics. */
class ModernApiGroupHierarchyTest extends ModernApiTestSupport {

    @Test
    void userGroupsListsDirectMemberships() {
        api().getGroupManager().createGroup("g1").save();
        var g2 = api().getGroupManager().createGroup("g2");
        g2.addParent("g1");
        g2.save();

        var user = api().getUserManager().createUser("hier-user");
        user.addGroup("g2");
        user.save();

        assertEquals(List.of("g2"), user.groups(PermissionContext.global(), false));
        assertEquals(List.of("g2"), user.groups(PermissionContext.global(), true));
        assertTrue(user.inGroup("g2", PermissionContext.global(), false));
    }

    @Test
    void groupParentTreeAndChildIdentifiers() {
        var root = api().getGroupManager().createGroup("root");
        var mid = api().getGroupManager().createGroup("mid");
        var leaf = api().getGroupManager().createGroup("leaf");
        mid.addParent(root.getName());
        leaf.addParent(mid.getName());
        leaf.save();

        assertTrue(mid.parentTree().contains(root.getName()));
        assertTrue(root.descendantIdentifiers().contains(leaf.getName()));
        assertEquals(root.childIdentifiers(), root.children().stream().map(Group::getName).toList());
    }

    @Test
    void groupMembersDirectVsInherited() {
        var parent = api().getGroupManager().createGroup("mem-parent");
        var child = api().getGroupManager().createGroup("mem-child");
        child.addParent(parent.getName());
        child.save();

        var directUser = api().getUserManager().createUser("direct-member");
        directUser.addGroup(parent.getName());
        directUser.save();

        var nestedUser = api().getUserManager().createUser("nested-member");
        nestedUser.addGroup(child.getName());
        nestedUser.save();

        assertTrue(parent.memberIdentifiers().contains(directUser.identifier()));
        assertFalse(parent.memberIdentifiers().contains(nestedUser.identifier()));

        assertEquals(1, parent.members(PermissionContext.global(), false).size());
        assertTrue(parent.members(PermissionContext.global(), true).stream().map(User::identifier).toList().contains(nestedUser.identifier()));
    }

    @Test
    void timedGroupMembership() {
        api().getGroupManager().createGroup("timed-hier-group").save();
        var user = api().getUserManager().createUser("timed-hier-user");
        user.addGroup("timed-hier-group", PermissionContext.global(), 60);
        user.save();

        var memberships = user.timedGroupMemberships();
        assertEquals(1, memberships.size());
        assertEquals("timed-hier-group", memberships.get(0).groupName());
        assertTrue(user.groupMembershipRemainingSeconds("timed-hier-group") > 0);
    }

    @Test
    void groupWeightAndDefaultFlags() {
        var group = api().getGroupManager().createGroup("flags-group");
        group.setWeight(42);
        group.setDefault(true);
        group.save();

        assertEquals(42, group.weight());
        assertTrue(group.isDefault());
    }
}
