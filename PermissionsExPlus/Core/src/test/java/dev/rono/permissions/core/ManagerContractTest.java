package dev.rono.permissions.core;

import lombok.var;

import static dev.rono.permissions.core.RuntimeFixture.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.exception.GroupAlreadyExistsException;
import dev.rono.permissions.api.exception.GroupNotFoundException;
import dev.rono.permissions.api.exception.InheritanceCycleException;
import dev.rono.permissions.api.exception.UserAlreadyExistsException;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.ladder.PromotionStatus;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionValue;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.manager.GroupManagerImpl;
import dev.rono.permissions.core.manager.UserManagerImpl;
import dev.rono.permissions.core.model.GroupSnapshot;
import dev.rono.permissions.core.model.UserSnapshot;
import dev.rono.permissions.core.resolver.ResolverImpl;
import dev.rono.permissions.core.store.SnapshotCodec;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dev.rono.permissions.core.util.Immutables;
import dev.rono.permissions.core.util.Optionals;

class ManagerContractTest {
    private RuntimeFixture runtime;

    @BeforeEach
    void setUp() {
        runtime = new RuntimeFixture();
    }

    @Test
    void groupCrudIsCaseNormalizedAndRejectsDuplicates() {
        var group = await(runtime.groups.create("Staff"));

        assertEquals("staff", group.name());
        assertTrue(await(runtime.groups.find("STAFF")).isPresent());
        assertCause(GroupAlreadyExistsException.class, () -> await(runtime.groups.create("staff")));
        assertTrue(await(runtime.groups.delete("STAFF")));
        assertFalse(await(runtime.groups.delete("staff")));
    }

    @Test
    void userCrudSupportsUuidAndCaseInsensitiveNameLookup() {
        var id = UUID.randomUUID();

        await(runtime.users.create(id, "Alex"));
        assertEquals(id, await(runtime.users.find("aLeX")).get().uniqueId());
        assertCause(UserAlreadyExistsException.class, () -> await(runtime.users.create(UUID.randomUUID(), "Alex")));
        assertTrue(await(runtime.users.delete(id)));
        assertFalse(await(runtime.users.delete(id)));
    }

    @Test
    void unloadedSubjectsCanBeLoadedFromStorage() {
        var id = UUID.randomUUID();

        await(runtime.users.create(id, "Alex"));

        runtime.users.cache().unload(id);

        assertFalse(runtime.users.cache().isCached(id));
        assertEquals("Alex", await(runtime.users.load(id)).name());
        assertTrue(runtime.users.cache().isCached(id));
    }

    @Test
    void inactiveEvictionNeverRemovesOnlineUsers() {
        var onlineId = UUID.randomUUID();
        var offlineId = UUID.randomUUID();
        var secondOfflineId = UUID.randomUUID();

        await(runtime.users.create(onlineId, "Online"));
        await(runtime.users.create(offlineId, "Offline"));
        await(runtime.users.create(secondOfflineId, "SecondOffline"));

        var limitedUsers = new UserManagerImpl(runtime.store, runtime.events, Duration.ofMinutes(10), 1);
        limitedUsers.attachGroups(runtime.groups);
        limitedUsers.cache().markOnline(onlineId);

        await(limitedUsers.load(onlineId));
        await(limitedUsers.load(offlineId));
        await(limitedUsers.load(secondOfflineId));

        var evicted = limitedUsers.cache().evictInactive();

        assertEquals(1, evicted.size());
        assertTrue(Immutables.setOf("Offline", "SecondOffline").contains(evicted.get(0).name()));
        assertTrue(limitedUsers.cache().isCached(onlineId));
        assertFalse(limitedUsers.cache().unload(onlineId));
        assertEquals(2, limitedUsers.cache().all().size());
    }

    @Test
    void markingAUserOfflineRetainsTheirProfileForLaterEviction() {
        var id = UUID.randomUUID();

        await(runtime.users.create(id, "Alex"));

        runtime.users.cache().markOnline(id);

        await(runtime.users.load(id));

        runtime.users.cache().markOffline(id);

        assertTrue(runtime.users.cache().isCached(id));
        assertTrue(runtime.users.cache().unload(id));
    }

    @Test
    void zeroOfflineCapacityLimitRetainsAllProfiles() {
        var unlimited = new UserManagerImpl(runtime.store, runtime.events, Duration.ofMinutes(10), 0);

        unlimited.attachGroups(runtime.groups);

        for (var index = 0; index < 20; index++) {
            var id = UUID.randomUUID();

            await(runtime.users.create(id, "User" + index));
            await(unlimited.load(id));
        }

        assertEquals(20, unlimited.cache().all().size());
        assertTrue(unlimited.cache().evictInactive().isEmpty());
    }

    @Test
    void inactiveOfflineProfilesExpireByAccessTime() throws InterruptedException {
        var expiring = new UserManagerImpl(runtime.store, runtime.events, Duration.ofMillis(1), 10);
        expiring.attachGroups(runtime.groups);

        var id = UUID.randomUUID();

        await(runtime.users.create(id, "Alex"));
        await(expiring.load(id));

        Thread.sleep(20);

        var evicted = expiring.cache().evictInactive();

        assertEquals(Immutables.listOf("Alex"), evicted.stream().map(User::name).collect(Immutables.toList()));
        assertFalse(expiring.cache().isCached(id));
    }

    @Test
    void expirationSweepsReportEveryRemovedNode() {
        var expired = Instant.now().minusSeconds(1);

        await(runtime.groups.create("default"));
        await(runtime.groups.create("staff"));
        await(runtime.groups.modify("staff", modifier -> {
            modifier.addParent(ParentNode.builder().group("default").expiry(expired).build());
            modifier.setPermission(PermissionNode.builder().permission("group.expired").expiry(expired).build());
            modifier.setOption(OptionNode.builder().option("prefix", "[Old]").expiry(expired).build());
        }));

        var id = UUID.randomUUID();

        await(runtime.users.create(id, "Alex"));
        await(runtime.users.modify(id, modifier -> {
            modifier.addGroup(ParentNode.builder().group("staff").expiry(expired).build());
            modifier.setPermission(PermissionNode.builder().permission("user.expired").expiry(expired).build());
            modifier.setOption(OptionNode.builder().option("suffix", "[Old]").expiry(expired).build());
        }));

        var removals = new ArrayList<>(runtime.users.purgeExpired());
        removals.addAll(runtime.groups.purgeExpired());

        assertEquals(6, removals.size());
        assertEquals(Immutables.setOf("group membership", "permission", "option", "parent"), removals.stream().map(removal -> removal.nodeType()).collect(Collectors.toSet()));
        assertTrue(removals.stream().anyMatch(removal -> removal.subject().startsWith("Alex (")));
        assertTrue(await(runtime.users.load(id)).groups().isEmpty());
        assertTrue(await(runtime.users.load(id)).explicitPermissions().isEmpty());
        assertTrue(await(runtime.users.load(id)).explicitOptions().isEmpty());
        assertTrue(await(runtime.groups.load("staff")).parents().isEmpty());
        assertTrue(await(runtime.groups.load("staff")).explicitPermissions().isEmpty());
        assertTrue(await(runtime.groups.load("staff")).explicitOptions().isEmpty());
    }

    @Test
    void shorthandMutationsPreserveNodeDataAndPersistOnlyFlatPermissions() {
        var contexts = ContextSet.builder().add("world", "nether").build();
        var expiry = Instant.now().plusSeconds(60);
        var id = UUID.randomUUID();

        await(runtime.users.create(id, "Alex"));

        var user = await(runtime.users.modify(id, modifier -> modifier.setPermission(PermissionNode.builder()
                .permission("network.{survival,skyblock}.kit.{1-3}").value(PermissionValue.DENY).contexts(contexts)
                .expiry(expiry).build())));

        assertEquals(6, user.explicitPermissions().size());
        assertTrue(user.explicitPermissions().stream().allMatch(node -> node.value() == PermissionValue.DENY && node.contexts().equals(contexts) && node.expiry().get().equals(expiry)));
        assertTrue(user.explicitPermissions().stream().noneMatch(node -> node.permission().contains("{")));

        var persisted = SnapshotCodec.user(runtime.store.get("users", id.toString()).get());

        assertEquals(user.explicitPermissions(), persisted.explicitPermissions());
    }

    @Test
    void storageHydrationExpandsUsersAndGroupsBeforeCaching() {
        var userId = UUID.randomUUID();
        var rawPermission = PermissionNode.builder().permission("example.{one,two}").build();

        runtime.store.put("users", userId.toString(), SnapshotCodec.user(new UserSnapshot(userId, "Alex", Immutables.setOf(rawPermission), Immutables.setOf(), Immutables.setOf())));
        runtime.store.put("groups", "staff", SnapshotCodec.group(new GroupSnapshot("staff", OptionalInt.empty(), Immutables.setOf(rawPermission), Immutables.setOf(), Immutables.setOf())));

        var users = new UserManagerImpl(runtime.store, runtime.events, Duration.ofMinutes(10), 10, Runnable::run, true);
        users.attachGroups(runtime.groups);

        var groups = new GroupManagerImpl(runtime.store, runtime.events, 10, Runnable::run, true);
        groups.loadAll();

        assertEquals(Immutables.setOf("example.one", "example.two"), await(users.load(userId)).explicitPermissions().stream().map(PermissionNode::permission).collect(Collectors.toSet()));
        assertEquals(Immutables.setOf("example.one", "example.two"), groups.cache().get("staff").get().explicitPermissions().stream().map(PermissionNode::permission).collect(Collectors.toSet()));
    }

    @Test
    void disabledShorthandExpansionKeepsStoredPermissionsLiteral() {
        var id = UUID.randomUUID();
        var rawPermission = PermissionNode.builder().permission("example.{one,two}").build();

        runtime.store.put("users", id.toString(), SnapshotCodec.user(new UserSnapshot(id, "Alex", Immutables.setOf(rawPermission), Immutables.setOf(), Immutables.setOf())));

        var users = new UserManagerImpl(runtime.store, runtime.events, Duration.ofMinutes(10), 10, Runnable::run, false);
        users.attachGroups(runtime.groups);

        assertEquals(Immutables.setOf("example.{one,two}"), await(users.load(id)).explicitPermissions().stream().map(PermissionNode::permission).collect(Collectors.toSet()));
    }

    @Test
    void mutationsRejectMissingParents() {
        await(runtime.groups.create("child"));

        assertCause(GroupNotFoundException.class, () -> await(runtime.groups.modify("child", modifier -> modifier.addParent("missing"))));
    }

    @Test
    void inheritanceCyclesAreRejected() {
        await(runtime.groups.create("a"));
        await(runtime.groups.create("b"));
        await(runtime.groups.modify("a", modifier -> modifier.addParent("b")));

        assertCause(InheritanceCycleException.class, () -> await(runtime.groups.modify("b", modifier -> modifier.addParent("a"))));
    }

    @Test
    void ladderValidatesDuplicatesAndMissingGroups() {
        await(runtime.groups.create("member"));
        await(runtime.ladders.create("main"));

        assertCause(GroupNotFoundException.class, () -> await(runtime.ladders.modify("main", modifier -> modifier.add("missing"))));

        var ladder = await(runtime.ladders.modify("main", modifier -> modifier.setGroups(Immutables.listOf("member", "member"))));

        assertEquals(Immutables.listOf("member"), ladder.groups());
    }

    @Test
    void ladderMoveAndRemovePreserveOrder() {
        await(runtime.groups.create("one"));
        await(runtime.groups.create("two"));
        await(runtime.groups.create("three"));
        await(runtime.ladders.create("main"));
        await(runtime.ladders.modify("main", modifier -> modifier.setGroups(Immutables.listOf("one", "two", "three"))));

        var moved = await(runtime.ladders.modify("main", modifier -> modifier.move("three", 0)));

        assertEquals(Immutables.listOf("three", "one", "two"), moved.groups());

        var removed = await(runtime.ladders.modify(moved, modifier -> modifier.remove("one")));

        assertEquals(Immutables.listOf("three", "two"), removed.groups());
    }

    @Test
    void promotionReportsBoundaryAndMembershipStatuses() {
        await(runtime.groups.create("member"));
        await(runtime.groups.create("staff"));
        await(runtime.ladders.create("main"));
        await(runtime.ladders.modify("main", modifier -> modifier.setGroups(Immutables.listOf("member", "staff"))));

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));

        assertEquals(PromotionStatus.NOT_ON_LADDER, await(runtime.ladders.promote(user, "main")).status());

        user = await(runtime.users.modify(user, modifier -> modifier.addGroup("staff")));

        assertEquals(PromotionStatus.ALREADY_TOP, await(runtime.ladders.promote(user, "main")).status());
        assertEquals(PromotionStatus.DEMOTED, await(runtime.ladders.demote(user, "main")).status());
    }

    @Test
    void configuredDefaultIsResolvedInMemoryWithoutPersistedPolicy() {
        await(runtime.groups.create("guest"));

        var resolver = new ResolverImpl(runtime.groups, 10, false, true, true, "guest");

        assertEquals(Immutables.listOf("guest"), Optionals.stream(resolver.defaultGroups().resolve()).map(Group::name).collect(Immutables.toList()));
        assertTrue(runtime.store.all("defaults").isEmpty());
    }

    private static void assertCause(Class<? extends Throwable> expected, Runnable action) {
        var error = assertThrows(CompletionException.class, action::run);

        assertInstanceOf(expected, error.getCause());
    }
}
