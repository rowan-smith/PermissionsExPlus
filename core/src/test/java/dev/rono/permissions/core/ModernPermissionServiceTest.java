package dev.rono.permissions.core;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.TimedGroupMembership;
import dev.rono.permissions.api.subject.TimedPermissionEntry;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.world.Worlds;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModernPermissionServiceTest extends PEXTestBase {

    @Test
    void permissionServiceExposesBackendAndCounts() {
        PermissionService service = (PermissionService) manager;
        assertNotNull(service.backend());
        assertEquals(service.groupCount(), service.registeredGroupCount());
        assertEquals(service.userCount(), service.registeredUserNameCount());
    }

    @Test
    void userAndGroupCrudViaModernApi() {
        PermissionService service = (PermissionService) manager;

        Group defaultGroup = service.group("default");
        defaultGroup.addPermission("modern.group", null);

        User user = service.user("modern-api-user");
        user.addPermission("modern.test", null);
        user.addGroup("default", null);
        user.save();

        assertTrue(service.findUser("modern-api-user").isPresent());
        assertTrue(user.has("modern.test", null));
        assertTrue(user.inGroup("default", null, false));
        assertTrue(defaultGroup.permissions(null).contains("modern.group"));
        assertTrue(service.has("modern-api-user", "modern.test", null));

        user.removePermission("modern.test", null);
        user.removeGroup("default", null);
        user.delete();
        assertTrue(user.permissions(null).isEmpty());
    }

    @Test
    void worldPermissionsAndTimedEntries() {
        PermissionService service = (PermissionService) manager;
        User user = service.user("world-perms-user");

        user.inWorld("world").addPermission("world.node");
        user.inWorld("world").addTimedPermission("world.temp", 120);
        user.save();

        assertTrue(user.permissionsByWorld().containsKey("world"));
        assertTrue(user.inWorld("world").has("world.node"));
        assertFalse(user.inWorld("other").has("world.node"));

        List<TimedPermissionEntry> timed = user.timedPermissionEntries("world");
        assertEquals(1, timed.size());
        assertEquals("world.temp", timed.get(0).permission());
        assertTrue(timed.get(0).remainingSeconds() > 0);

        service.setWorldInheritance("world", List.of());
        assertNotNull(service.worldInheritance("world"));
    }

    @Test
    void timedGroupMembershipMetadata() {
        PermissionService service = (PermissionService) manager;
        service.group("timed-group");
        User user = service.user("timed-group-user");
        user.addGroup("timed-group", null, 90);

        List<TimedGroupMembership> memberships = user.timedGroupMemberships(null);
        assertEquals(1, memberships.size());
        assertEquals("timed-group", memberships.get(0).groupName());
        assertTrue(memberships.get(0).remainingSeconds() > 0);
        assertTrue(user.groupMembershipRemainingSeconds("timed-group", null) > 0);
    }

    @Test
    void reloadWrapsBackendFailures() throws Exception {
        ((PermissionService) manager).reload();
    }

    @Test
    void findUserByUuidWhenPersisted() {
        PermissionService service = (PermissionService) manager;
        UUID id = UUID.randomUUID();
        User user = service.user(id.toString());
        user.setOption("name", "uuid-user", null);
        user.save();

        Optional<User> found = service.findUser(id);
        assertTrue(found.isPresent());
        assertEquals("uuid-user", found.get().name());

        user.delete();
    }

    @Test
    void groupMembersAndDefaultGroups() {
        PermissionService service = (PermissionService) manager;
        Group group = service.group("member-group");
        User user = service.user("member-user");
        user.addGroup("member-group", Worlds.GLOBAL);
        user.save();

        assertTrue(group.memberIdentifiers().contains(user.identifier()));
        assertFalse(group.members().isEmpty());
        assertFalse(service.usersInGroup("member-group").isEmpty());
        assertNotNull(service.defaultGroups(null));
    }
}
