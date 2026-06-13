package dev.rono.permissions.core;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.permission.PermissionAddRequest;
import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.user.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ModernApiTest extends ModernApiTestSupport {

  @Test
  void userAndGroupCrudViaManagers() {
    manager.getGroup("default");
    var defaultGroup = api().getGroupManager().getGroup("default");
    defaultGroup.addPermission("modern.group");

    var user = api().getUserManager().createUser("modern-api-user");
    user.addPermission("modern.test");
    user.addGroup("default");
    user.save();

    assertTrue(api().getUserManager().findUser("modern-api-user").isPresent());
    assertTrue(user.has("modern.test"));
    assertTrue(user.inGroup("default", PermissionContext.global(), false));
    assertTrue(defaultGroup.permissions().contains("modern.group"));

    user.removePermission("modern.test");
    user.removeGroup("default");
    user.delete();
    assertTrue(user.permissions().isEmpty());
  }

  @Test
  void globalVsWorldPermissions() {
    var user = api().getUserManager().createUser("world-perms-user");

    user.inContext(PermissionContext.world("world")).addPermission("world.node");
    user.inContext(PermissionContext.world("world")).addTimedPermission("world.temp", 120);
    user.save();

    assertTrue(user.permissionsByRealm().containsKey("world"));
    assertTrue(user.inContext(PermissionContext.world("world")).has("world.node"));
    assertFalse(user.inContext(PermissionContext.world("other")).has("world.node"));
    assertFalse(user.has("world.node"));

    var timed = user.timedPermissionEntries(PermissionContext.world("world"));
    assertEquals(1, timed.size());
    assertEquals("world.temp", timed.get(0).permission());

    manager.setWorldInheritance("world", List.of());
    assertNotNull(manager.getWorldInheritance("world"));
  }

  @Test
  void timedGroupMembershipMetadata() {
    api().getGroupManager().createGroup("timed-group");
    var user = api().getUserManager().createUser("timed-group-user");
    user.addGroup("timed-group", PermissionContext.global(), 90);

    var memberships = user.timedGroupMemberships();
    assertEquals(1, memberships.size());
    assertEquals("timed-group", memberships.get(0).groupName());
  }

  @Test
  void findUserByUuidWhenPersisted() {
    var id = UUID.randomUUID();
    var user = api().getUserManager().createUser(id);
    user.setOption("name", "uuid-user");
    user.save();

    var found = api().getUserManager().findUser(id);
    assertTrue(found.isPresent());
    assertEquals("uuid-user", found.get().name());

    user.delete();
  }

  @Test
  void findUserGetHasPermission() {
    var user = api().getUserManager().createUser("find-get-user");
    user.addPermission("find.get.node");
    user.save();

    assertTrue(api().getUserManager().getUser("find-get-user").has("find.get.node"));

    user.delete();
  }

  @Test
  void permissionContextStorageRealm() {
    var resolver = new dev.rono.permissions.api.runtime.BukkitContextResolver();
    assertTrue(resolver.storageRealm(PermissionContext.global()).isEmpty());
    assertEquals("survival", resolver.storageRealm(PermissionContext.of("survival", null, null, null)).orElseThrow());
    assertEquals("proxy-1", resolver.storageRealm(PermissionContext.server("proxy-1")).orElseThrow());
  }

  @Test
  void groupMembers() {
    var group = api().getGroupManager().createGroup("member-group");
    var user = api().getUserManager().createUser("member-user");
    user.addGroup("member-group");
    user.save();

    assertTrue(group.memberIdentifiers().contains(user.identifier()));
    assertFalse(group.members().isEmpty());
    assertEquals(List.of(user.identifier()), group.members().stream().map(User::identifier).toList());
  }

  @Test
  void groupChildIdentifiers() {
    var parent = api().getGroupManager().createGroup("parent-id-group");
    var child = api().getGroupManager().createGroup("child-id-group");
    child.addParent(parent.getName());
    child.save();

    assertTrue(parent.childIdentifiers().contains(child.getName()));
    assertTrue(parent.descendantIdentifiers().contains(child.getName()));
    assertEquals(parent.childIdentifiers(), parent.children().stream().map(Group::getName).toList());
  }

  @Test
  void eventBusNotifiesListeners() {
    var received = new java.util.concurrent.atomic.AtomicInteger(0);
    var subscription = api().getEventBus().subscribe(new dev.rono.permissions.api.event.PermissionEventListener() {
      @Override
      public void onEntity(dev.rono.permissions.api.bus.EntityDispatch dispatch) {
        received.incrementAndGet();
      }
    });
    var user = api().getUserManager().createUser("event-bus-user");
    user.addPermission("event.test");
    user.save();
    assertTrue(received.get() > 0);
    api().getEventBus().unsubscribe(subscription);
  }

  @Test
  void groupChildrenAndReload() throws Exception {
    api().getGroupManager().createGroup("parent-group");
    var child = api().getGroupManager().createGroup("child-group");
    child.addParent("parent-group");
    child.save();
    var parent = api().getGroupManager().getGroup("parent-group");
    assertFalse(parent.children().isEmpty());
    assertFalse(parent.descendants().isEmpty());
    manager.reset();
  }

  @Test
  void promoteDemoteViaLadderManager() throws dev.rono.permissions.api.RankingException {
    var mod = api().getGroupManager().createGroup("mod");
    mod.setRank(2, "default");
    mod.save();
    var admin = api().getGroupManager().createGroup("admin");
    admin.setRank(1, "default");
    admin.save();
    var user = api().getUserManager().createUser("rank-user");
    user.addGroup("mod");
    user.save();

    var ladders = api().getLadderManager();
    var ladder = ladders.getLadder("default");
    assertEquals("admin", ladders.promote(user, ladder).getName());
    assertEquals("mod", ladders.demote(user, ladder).getName());
  }

  @Test
  void managerLifecycleAndHolderPermissions() {
    var api = api();
    var uuid = UUID.randomUUID();

    assertFalse(api.getUserManager().exists(uuid));
    assertTrue(api.getUserManager().findUser(uuid).isEmpty());

    var created = api.getUserManager().createUser(uuid);
    assertEquals(uuid, created.getId());
    assertTrue(api.getUserManager().exists(uuid));
    assertEquals(created.getId(), api.getUserManager().getUser(uuid).getId());

    api.getPermissionManager().addPermission(created.asHolder(), "lifecycle.test");
    assertTrue(api.getPermissionManager().hasPermission(created.asHolder(), "lifecycle.test"));
    assertSame(manager, api.getPermissionManager());

    api.getPermissionManager().addPermission(
        PermissionAddRequest.builder()
            .holder(created.asHolder())
            .permission("world.ctx.node")
            .addContext("world", "ctx-world")
            .build());
    assertTrue(api.getPermissionManager().hasPermission(
        created.asHolder(), "world.ctx.node", Map.of("world", "ctx-world")));
    assertFalse(api.getPermissionManager().hasPermission(
        created.asHolder(), "world.ctx.node", Map.of("world", "other-world")));

    assertThrows(dev.rono.permissions.api.user.UserAlreadyExistsException.class,
        () -> api.getUserManager().createUser(uuid));
  }

  @Test
  void managerCounts() {
    var api = api();

    var user = api.getUserManager().createUser(UUID.randomUUID());
    assertEquals(1, api.getUserManager().count());
    assertEquals(1, api.getUserManager().count(u -> u.getId().equals(user.getId())));
    assertEquals(0, api.getUserManager().count(u -> u.getName().equals("nonexistent-name")));

    var group = api.getGroupManager().createGroup("count-test-group");
    assertTrue(api.getGroupManager().count() >= 1);
    assertEquals(1, api.getGroupManager().count(g -> g.getName().equals(group.getName())));

    api.getWorldManager().createWorld("count-test-world");
    assertTrue(api.getWorldManager().count() >= 1);
    assertEquals(1, api.getWorldManager().count(w -> w.getName().equals("count-test-world")));

    assertTrue(api.getLadderManager().count() >= 1);
    assertTrue(api.getLadderManager().count(l -> l.getName().equals("default")) >= 1);
  }
}
