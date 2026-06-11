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
        assertNotNull(service.query().backend().info());
        assertEquals(service.query().groups().count(), service.registeredGroupCount());
        assertEquals(service.query().users().count(), service.registeredUserNameCount());
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
        assertTrue(user.has("modern.test", null));

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
        assertFalse(group.members(Worlds.GLOBAL, true).isEmpty());
        assertNotNull(service.defaultGroups(null));
    }

    @Test
    void eventBusNotifiesListeners() {
        PermissionService service = (PermissionService) manager;
        var received = new java.util.concurrent.atomic.AtomicInteger(0);
        var subscription = service.events().subscribe(new dev.rono.permissions.api.event.PermissionEventListener() {
            @Override
            public void onEntity(dev.rono.permissions.api.bus.EntityDispatch dispatch) {
                received.incrementAndGet();
            }
        });
        service.user("event-bus-user").addPermission("event.test", null);
        service.user("event-bus-user").save();
        assertTrue(received.get() > 0);
        service.events().unsubscribe(subscription);
    }

    @Test
    void editSessionBatchSave() {
        PermissionService service = (PermissionService) manager;
        try (var session = service.openEditSession()) {
            session.editUser("batch-user", user -> user.addPermission("batch.node", null));
            session.editGroup("batch-group", group -> group.addPermission("batch.group", null));
            session.save();
        }
        assertTrue(service.findUser("batch-user").isPresent());
        assertTrue(service.group("batch-group").permissions(null).contains("batch.group"));
    }

    @Test
    void groupChildrenAndAsyncReload() throws Exception {
        PermissionService service = (PermissionService) manager;
        Group parent = service.group("parent-group");
        Group child = service.group("child-group");
        child.addParent("parent-group", null);
        child.save();
        assertFalse(parent.children().isEmpty());
        assertFalse(parent.descendants().isEmpty());
        service.reloadAsync().get();
    }

    @Test
    void promoteDemoteViaModernUser() throws dev.rono.permissions.api.RankingException {
        PermissionService service = (PermissionService) manager;
        Group mod = service.group("mod");
        mod.setRank(2, "default");
        mod.save();
        Group admin = service.group("admin");
        admin.setRank(1, "default");
        admin.save();
        User user = service.user("rank-user");
        user.addGroup("mod", null);
        user.save();

        Group promoted = user.promote("default");
        assertEquals("admin", promoted.identifier());
        Group demoted = user.demote("default");
        assertEquals("mod", demoted.identifier());
    }

    @Test
    void queryApiEntryPoints() {
        PermissionService service = (PermissionService) manager;
        service.group("member-group");
        User user = service.user("fluent-user");
        user.addPermission("fluent.test", null);
        user.addGroup("member-group", Worlds.GLOBAL);
        user.save();

        assertTrue(service.query().users().resolve("fluent-user").inWorld(null).has("fluent.test"));
        assertTrue(service.query().world(null).user("fluent-user").inGroup("member-group"));
        assertFalse(service.query().groups().resolve("member-group").inWorld(null).members().isEmpty());
        assertTrue(service.query().world(null).findUser("fluent-user").map(u -> u.inGroup("member-group")).orElse(false));
        assertTrue(service.query().world(null).findGroup("member-group").map(g -> !g.members().isEmpty()).orElse(false));
        assertTrue(service.query().users().count() > 0);
        assertTrue(service.query().groups().count() > 0);

        service.group("fluent-parent");
        Group child = service.group("fluent-child");
        child.addParent("fluent-parent", null);
        child.save();
        assertFalse(service.query().world(null).group("fluent-parent").children().isEmpty());
        assertFalse(service.query().world(null).group("fluent-parent").descendants().isEmpty());

        user.delete();
    }

    @Test
    void exportDataAndBackendHandle() throws dev.rono.permissions.api.PermissionsExException {
        PermissionService service = (PermissionService) manager;
        assertNotNull(service.query().backend().exportData());
        var handle = service.query().backend().createHandle("mock");
        assertNotNull(handle.info());
    }
}
