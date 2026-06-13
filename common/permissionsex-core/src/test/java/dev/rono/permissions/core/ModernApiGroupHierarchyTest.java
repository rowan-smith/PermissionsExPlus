package dev.rono.permissions.core;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.world.Worlds;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Group membership graph, parent trees, and inherit flag semantics. */
class ModernApiGroupHierarchyTest extends ModernApiTestSupport {

    @Test
    void userGroupsListsDirectMemberships() {
        api().getGroupManager().createGroup("g1").save();
        var g2 = api().getGroupManager().createGroup("g2");
        g2.addParent("g1", Worlds.GLOBAL);
        g2.save();

        var user = api().getUserManager().createUser("hier-user");
        user.addGroup("g2", Worlds.GLOBAL);
        user.save();

        assertEquals(List.of("g2"), user.groups(Worlds.GLOBAL, false));
        assertEquals(List.of("g2"), user.groups(Worlds.GLOBAL, true));
        assertTrue(user.inGroup("g2", Worlds.GLOBAL, false));
    }

    @Test
    void groupParentTreeAndChildIdentifiers() {
        var root = api().getGroupManager().createGroup("root");
        var mid = api().getGroupManager().createGroup("mid");
        var leaf = api().getGroupManager().createGroup("leaf");
        mid.addParent(root.getName(), Worlds.GLOBAL);
        leaf.addParent(mid.getName(), Worlds.GLOBAL);
        leaf.save();

        assertTrue(mid.parentTree().contains(root.getName()));
        assertTrue(root.descendantIdentifiers().contains(leaf.getName()));
        assertEquals(root.childIdentifiers(Worlds.GLOBAL), root.children().stream().map(Group::getName).toList());
    }

    @Test
    void groupMembersDirectVsInherited() {
        var parent = api().getGroupManager().createGroup("mem-parent");
        var child = api().getGroupManager().createGroup("mem-child");
        child.addParent(parent.getName(), Worlds.GLOBAL);
        child.save();

        var directUser = api().getUserManager().createUser("direct-member");
        directUser.addGroup(parent.getName(), Worlds.GLOBAL);
        directUser.save();

        var nestedUser = api().getUserManager().createUser("nested-member");
        nestedUser.addGroup(child.getName(), Worlds.GLOBAL);
        nestedUser.save();

        assertTrue(parent.memberIdentifiers().contains(directUser.identifier()));
        assertFalse(parent.memberIdentifiers().contains(nestedUser.identifier()));

        assertEquals(1, parent.members(Worlds.GLOBAL, false).size());
        assertTrue(parent.members(Worlds.GLOBAL, true).stream().map(User::identifier).toList().contains(nestedUser.identifier()));
    }

    @Test
    void timedGroupMembership() {
        api().getGroupManager().createGroup("timed-hier-group").save();
        var user = api().getUserManager().createUser("timed-hier-user");
        user.addGroup("timed-hier-group", Worlds.GLOBAL, 60);
        user.save();

        var memberships = user.timedGroupMemberships(Worlds.GLOBAL);
        assertEquals(1, memberships.size());
        assertEquals("timed-hier-group", memberships.get(0).groupName());
        assertTrue(user.groupMembershipRemainingSeconds("timed-hier-group") > 0);
    }

    @Test
    void groupWeightAndDefaultFlags() {
        var group = api().getGroupManager().createGroup("flags-group");
        group.setWeight(42);
        group.setDefault(true, Worlds.GLOBAL);
        group.save();

        assertEquals(42, group.weight());
        assertTrue(group.isDefault());
    }
}
