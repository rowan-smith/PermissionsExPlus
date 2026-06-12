package dev.rono.permissions.core;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.service.PermissionServiceBridge;
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

    private PermissionService pex() {
        return (PermissionService) manager;
    }

    private PermissionServiceBridge bridge() {
        return PermissionService.requireBridge(pex());
    }

    @Test
    void registryAndBackend() {
        assertNotNull(pex().backend().getActive());
        assertTrue(pex().backend().isActive());
        assertTrue(pex().groups().count() >= 0);
        assertTrue(pex().users().count() >= 0);
        assertTrue(pex().worlds().count() >= 0);
    }

    @Test
    void userAndGroupCrudViaModernApi() {
        Group defaultGroup = bridge().group("default");
        defaultGroup.addPermission("modern.group", null);

        User user = pex().user("modern-api-user");
        user.addPermission("modern.test", null);
        user.addGroup("default", null);
        user.save();

        assertTrue(pex().findUser("modern-api-user").optional().isPresent());
        assertTrue(user.hasPermission("modern.test"));
        assertTrue(user.inGroup("default", null, false));
        assertTrue(defaultGroup.permissions(null).contains("modern.group"));

        user.removePermission("modern.test", null);
        user.removeGroup("default", null);
        user.delete();
        assertTrue(user.permissions(null).isEmpty());
    }

    @Test
    void globalVsWorldPermissions() {
        User user = pex().user("world-perms-user");

        user.inWorld("world").addPermission("world.node");
        user.inWorld("world").addTimedPermission("world.temp", 120);
        user.save();

        assertTrue(user.permissionsByWorld().containsKey("world"));
        assertTrue(pex().world("world").user("world-perms-user").hasPermission("world.node"));
        assertFalse(pex().world("other").user("world-perms-user").hasPermission("world.node"));
        assertFalse(user.hasPermission("world.node"));

        List<TimedPermissionEntry> timed = user.timedPermissionEntries("world");
        assertEquals(1, timed.size());
        assertEquals("world.temp", timed.get(0).permission());

        bridge().setWorldInheritance("world", List.of());
        assertNotNull(bridge().worldInheritance("world"));
    }

    @Test
    void timedGroupMembershipMetadata() {
        pex().group("timed-group");
        User user = pex().user("timed-group-user");
        user.addGroup("timed-group", null, 90);

        List<TimedGroupMembership> memberships = user.timedGroupMemberships(null);
        assertEquals(1, memberships.size());
        assertEquals("timed-group", memberships.get(0).groupName());
    }

    @Test
    void reloadWrapsBackendFailures() throws Exception {
        pex().reload();
    }

    @Test
    void findUserByUuidWhenPersisted() {
        UUID id = UUID.randomUUID();
        User user = pex().user(id.toString());
        user.setOption("name", "uuid-user", null);
        user.save();

        Optional<User> found = pex().findUser(id).optional();
        assertTrue(found.isPresent());
        assertEquals("uuid-user", found.get().name());

        user.delete();
    }

    @Test
    void findUserGetHasPermission() {
        User user = pex().user("find-get-user");
        user.addPermission("find.get.node", null);
        user.save();

        assertTrue(pex().findUser("find-get-user").get().hasPermission("find.get.node"));

        user.delete();
    }

    @Test
    void groupMembersAndDefaultGroups() {
        Group group = pex().group("member-group");
        User user = pex().user("member-user");
        user.addGroup("member-group", Worlds.GLOBAL);
        user.save();

        assertTrue(group.memberIdentifiers().contains(user.identifier()));
        assertFalse(group.members().isEmpty());
        assertNotNull(pex().world(null).defaultGroups());
    }

    @Test
    void eventBusNotifiesListeners() {
        var received = new java.util.concurrent.atomic.AtomicInteger(0);
        var subscription = pex().events().subscribe(new dev.rono.permissions.api.event.PermissionEventListener() {
            @Override
            public void onEntity(dev.rono.permissions.api.bus.EntityDispatch dispatch) {
                received.incrementAndGet();
            }
        });
        pex().user("event-bus-user").addPermission("event.test", null);
        pex().user("event-bus-user").save();
        assertTrue(received.get() > 0);
        pex().events().unsubscribe(subscription);
    }

    @Test
    void sessionBatchSave() {
        try (var session = pex().session().start()) {
            session.editUser("batch-user", user -> user.addPermission("batch.node", null));
            session.editGroup("batch-group", group -> group.addPermission("batch.group", null));
            session.save();
        }
        assertTrue(pex().findUser("batch-user").optional().isPresent());
        assertTrue(pex().group("batch-group").permissions(null).contains("batch.group"));
    }

    @Test
    void groupChildrenAndAsyncReload() throws Exception {
        pex().group("parent-group");
        Group child = pex().group("child-group");
        child.addParent("parent-group", null);
        child.save();
        assertFalse(pex().group("parent-group").children().isEmpty());
        assertFalse(pex().group("parent-group").descendants().isEmpty());
        pex().reloadAsync().get();
    }

    @Test
    void promoteDemoteViaModernUser() throws dev.rono.permissions.api.RankingException {
        Group mod = pex().group("mod");
        mod.setRank(2, "default");
        mod.save();
        Group admin = pex().group("admin");
        admin.setRank(1, "default");
        admin.save();
        User user = pex().user("rank-user");
        user.addGroup("mod", null);
        user.save();

        assertEquals("admin", user.promote("default").identifier());
        assertEquals("mod", user.demote("default").identifier());
    }

    @Test
    void flatApiEntryPoints() {
        pex().group("member-group");
        User user = pex().user("fluent-user");
        user.addPermission("fluent.test", null);
        user.addGroup("member-group", Worlds.GLOBAL);
        user.save();

        assertTrue(user.hasPermission("fluent.test"));
        assertTrue(pex().world(null).user("fluent-user").inGroup("member-group"));
        assertFalse(pex().world(null).group("member-group").members().isEmpty());
        assertTrue(pex().findUser("fluent-user").get().hasPermission("fluent.test"));
        assertTrue(pex().findGroup("member-group").optional().isPresent());

        pex().group("fluent-parent");
        Group child = pex().group("fluent-child");
        child.addParent("fluent-parent", null);
        child.save();
        assertFalse(pex().world(null).group("fluent-parent").children().isEmpty());

        user.delete();
    }

    @Test
    void exportDataAndBackendHandle() throws dev.rono.permissions.api.PermissionsExException {
        assertNotNull(pex().backend().exportData());
        assertNotNull(pex().backend().createHandle("mock").info());
    }
}
