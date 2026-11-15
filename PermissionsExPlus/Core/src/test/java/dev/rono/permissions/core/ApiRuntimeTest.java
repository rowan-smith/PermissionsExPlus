package dev.rono.permissions.core;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.exception.InheritanceCycleException;
import dev.rono.permissions.api.event.user.UserModifiedEvent;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.ladder.PromotionStatus;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.core.config.AdvancedConfiguration;
import dev.rono.permissions.core.context.ContextManagerImpl;
import dev.rono.permissions.core.event.EventBusImpl;
import dev.rono.permissions.core.manager.GroupManagerImpl;
import dev.rono.permissions.core.manager.LadderManagerImpl;
import dev.rono.permissions.core.manager.UserManagerImpl;
import dev.rono.permissions.core.resolver.ResolverImpl;
import dev.rono.permissions.core.placeholder.PlaceholderApiService;
import dev.rono.permissions.core.store.MemoryDataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import dev.rono.permissions.core.util.Immutables;

class ApiRuntimeTest {
    MemoryDataStore store;
    GroupManagerImpl groups;
    UserManagerImpl users;
    LadderManagerImpl ladders;
    ResolverImpl resolvers;

    @BeforeEach
    void setUp() {
        store = new MemoryDataStore();
        store.open();

        var events = new EventBusImpl(error -> fail(error));

        groups = new GroupManagerImpl(store, events, 10);
        users = new UserManagerImpl(store, events);
        ladders = new LadderManagerImpl(store, events);

        groups.attach(users, ladders);
        users.attachGroups(groups);
        ladders.attach(users, groups);

        resolvers = new ResolverImpl(groups, 10);
    }

    @Test
    void modifiesImmutableSnapshotsAndPublishesOneEvent() {
        var count = new AtomicInteger();
        var events = new EventBusImpl(error -> fail(error));

        groups = new GroupManagerImpl(store, events, 10);
        users = new UserManagerImpl(store, events);
        ladders = new LadderManagerImpl(store, events);

        groups.attach(users, ladders);
        users.attachGroups(groups);
        ladders.attach(users, groups);

        events.subscribe(UserModifiedEvent.class, ignored -> count.incrementAndGet());

        var id = UUID.randomUUID();
        var original = users.create(id, "Rono").toCompletableFuture().join();
        var current = users.modify(id, modifier -> modifier.allowPermission("example.use")).toCompletableFuture().join();

        assertTrue(original.explicitPermissions().isEmpty());
        assertTrue(current.explicitlyAllows("example.use", ContextSet.empty()));
        assertEquals(1, count.get());
    }

    @Test
    void resolvesContextInheritanceWildcardsOptionsAndExplicitDeny() {
        groups.create("default").toCompletableFuture().join();

        groups.create("staff").toCompletableFuture().join();

        groups.modify("default", modifier -> {
            modifier.allowPermission("example.*");

            modifier.setPrefix("[Default]");
        }).toCompletableFuture().join();

        groups.modify("staff", modifier -> {
            modifier.addParent("default");
            modifier.setWeight(50);
            modifier.allowPermission("example.admin");
        }).toCompletableFuture().join();

        var id = UUID.randomUUID();

        var user = users.create(id, "Rono").toCompletableFuture().join();

        user = users.modify(user, modifier -> {
            modifier.addGroup("staff");
            modifier.denyPermission("example.blocked");
        }).toCompletableFuture().join();

        assertEquals(PermissionResult.ALLOW, resolvers.permissions().check(user, "example.admin", QueryOptions.global()));
        assertEquals(PermissionResult.ALLOW, resolvers.permissions().check(user, "example.other", QueryOptions.global()));
        assertEquals(PermissionResult.DENY, resolvers.permissions().check(user, "example.blocked", QueryOptions.global()));
        assertEquals("[Default]", resolvers.options().prefix(user, QueryOptions.global()).get());
        assertEquals("staff", resolvers.primaryGroup().resolve(user, QueryOptions.global()).get().name());
    }

    @Test
    void implicitDefaultOnlyAppliesWhenTheUserRecordHasNoAssignedGroups() {
        var survival = ContextSet.builder().add("world", "survival").build();

        groups.create("default").toCompletableFuture().join();
        groups.create("member").toCompletableFuture().join();

        groups.modify("default", modifier -> {
            modifier.allowPermission("build.fly");

            modifier.setPrefix("[Default]");
        }).toCompletableFuture().join();

        var user = users.create(UUID.randomUUID(), "Rono").toCompletableFuture().join();

        assertTrue(user.groups().isEmpty());
        assertEquals(PermissionResult.ALLOW, resolvers.permissions().check(user, "build.fly", survival));
        assertEquals("[Default]", resolvers.options().prefix(user, QueryOptions.global()).get());
        assertEquals(Immutables.setOf("default"), resolvers.inheritance().groups(user, QueryOptions.global()).stream().map(Group::name).collect(Collectors.toSet()));
        assertEquals("default", resolvers.primaryGroup().resolve(user, QueryOptions.global()).get().name());

        users.cache().unload(user.uniqueId());

        user = users.load(user.uniqueId()).toCompletableFuture().join();

        assertTrue(user.groups().isEmpty());

        var noDefaults = QueryOptions.builder().contexts(survival).includeDefaults(false).build();

        assertEquals(PermissionResult.UNDEFINED, resolvers.permissions().check(user, "build.fly", noDefaults));

        var hub = ContextSet.builder().add("world", "hub").build();

        user = users.modify(user, modifier -> modifier.addGroup("member", hub)).toCompletableFuture().join();

        assertEquals(PermissionResult.UNDEFINED, resolvers.permissions().check(user, "build.fly", survival));
    }

    @Test
    void ladderPromotionPersistsContextualMembership() {
        groups.create("member").toCompletableFuture().join();
        groups.create("moderator").toCompletableFuture().join();

        ladders.create("staff").toCompletableFuture().join();
        ladders.modify("staff", modifier -> modifier.setGroups(Immutables.listOf("member", "moderator"))).toCompletableFuture().join();

        var context = ContextSet.builder().add("server", "hub").build();
        var user = users.create(UUID.randomUUID(), "Rono").toCompletableFuture().join();

        users.modify(user, modifier -> modifier.addGroup("member", context)).toCompletableFuture().join();

        var result = ladders.promote(user.uniqueId(), "staff", context).toCompletableFuture().join();

        assertEquals(PromotionStatus.PROMOTED, result.status());
        assertEquals("moderator", result.newGroup().get());
        assertTrue(users.cache().get(user.uniqueId()).get().hasDirectGroup("moderator", context));
    }

    @Test
    void groupRenameAndDeleteRepairUnloadedPersistedReferences() {
        groups.create("member").toCompletableFuture().join();
        groups.create("child").toCompletableFuture().join();
        groups.modify("child", modifier -> modifier.addParent("member")).toCompletableFuture().join();

        ladders.create("main").toCompletableFuture().join();
        ladders.modify("main", modifier -> modifier.add("member")).toCompletableFuture().join();

        var id = UUID.randomUUID();

        users.create(id, "Rono").toCompletableFuture().join();
        users.modify(id, modifier -> modifier.addGroup("member")).toCompletableFuture().join();

        users.cache().unload(id);
        groups.cache().unload("child");
        ladders.cache().unload("main");

        groups.rename("member", "citizen").toCompletableFuture().join();

        assertTrue(users.load(id).toCompletableFuture().join().hasDirectGroup("citizen", ContextSet.empty()));
        assertTrue(groups.load("child").toCompletableFuture().join().hasDirectParent("citizen", ContextSet.empty()));
        assertTrue(ladders.load("main").toCompletableFuture().join().contains("citizen"));

        users.cache().unload(id);
        groups.cache().unload("child");
        ladders.cache().unload("main");

        groups.delete("citizen").toCompletableFuture().join();

        assertTrue(users.load(id).toCompletableFuture().join().groups().isEmpty());
        assertTrue(groups.load("child").toCompletableFuture().join().parents().isEmpty());
        assertTrue(ladders.load("main").toCompletableFuture().join().groups().isEmpty());
    }

    @Test
    void cycleDetectionTraversesUnloadedAncestors() {
        groups.create("a").toCompletableFuture().join();
        groups.create("b").toCompletableFuture().join();
        groups.create("c").toCompletableFuture().join();
        groups.modify("b", modifier -> modifier.addParent("c")).toCompletableFuture().join();
        groups.modify("c", modifier -> modifier.addParent("a")).toCompletableFuture().join();
        groups.cache().unload("c");

        var error = assertThrows(java.util.concurrent.CompletionException.class, () -> groups.modify("a", modifier -> modifier.addParent("b")).toCompletableFuture().join());

        assertInstanceOf(InheritanceCycleException.class, error.getCause());
    }

    @Test
    void runtimeContextsAndLegacyPlaceholderFamiliesRemainAvailable() {
        var advanced = new AdvancedConfiguration("none", 10, 30, "offline", "global", true, true, true, true, Immutables.mapOf("environment", "production"), 10, "localhost", 6379, "", "channel", 2000, true);
        var contexts = new ContextManagerImpl(advanced);

        assertTrue(contexts.staticContexts().contains("server", "global"));

        groups.create("member").toCompletableFuture().join();
        groups.create("admin").toCompletableFuture().join();
        groups.modify("member", modifier -> {
            modifier.setWeight(10);
            modifier.setOption("display-name", "Member");
            modifier.setOption("colour", "green");
        }).toCompletableFuture().join();
        groups.modify("admin", modifier -> modifier.setWeight(20)).toCompletableFuture().join();

        ladders.create("main").toCompletableFuture().join();
        ladders.modify("main", modifier -> modifier.setGroups(Immutables.listOf("member", "admin"))).toCompletableFuture().join();

        var id = UUID.randomUUID();

        users.create(id, "Rono").toCompletableFuture().join();
        users.modify(id, modifier -> {
            modifier.addGroup("member");
            modifier.setOption("prefix", "[Other]", ContextSet.builder().add("server", "other").build());
        }).toCompletableFuture().join();

        var placeholders = new PlaceholderApiService(users, groups, ladders, resolvers, contexts, store, "none");

        assertEquals("Member", placeholders.resolve(id, "user_groups_list_display"));
        assertEquals("green", placeholders.resolve(id, "group_option_member_colour"));
        assertEquals("1", placeholders.resolve(id, "group_member_count_member"));
        assertEquals("admin", placeholders.resolve(id, "user_ladder_next_group_main"));
        assertEquals("true", placeholders.resolve(id, "user_ladder_is_at_bottom_main"));
        assertEquals("production", placeholders.resolve(id, "server_context_environment_environment"));
        assertEquals("[Other]", placeholders.resolve(id, "user_prefix_server:other"));
    }
}
