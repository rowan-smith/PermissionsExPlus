package dev.rono.permissions.core;

import lombok.var;

import lombok.Value;
import lombok.experimental.Accessors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.CandidateStatus;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.config.MetaFormatting;
import dev.rono.permissions.core.config.PermissionConflictResolution;
import dev.rono.permissions.core.event.EventBusImpl;
import dev.rono.permissions.core.manager.GroupManagerImpl;
import dev.rono.permissions.core.manager.LadderManagerImpl;
import dev.rono.permissions.core.manager.UserManagerImpl;
import dev.rono.permissions.core.resolver.ResolverImpl;
import dev.rono.permissions.core.store.MemoryDataStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResolverConflictResolutionTest {
    @Test
    void denyWinsEqualPriorityConflictByDefault() {
        var fixture = fixture(PermissionConflictResolution.DENY_WINS, MetaFormatting.HIGHEST_WEIGHT);
        var user = conflictingUser(fixture, 50, 50);
        var explanation = fixture.resolver.permissions().explain(user, "example.fly", QueryOptions.global());

        assertEquals(PermissionResult.DENY, explanation.result());
        assertEquals("deny", ((Group) explanation.winner().get().source()).name());
    }

    @Test
    void trueWinsEqualPriorityConflictWhenConfigured() {
        var fixture = fixture(PermissionConflictResolution.TRUE_WINS, MetaFormatting.HIGHEST_WEIGHT);
        var user = conflictingUser(fixture, 50, 50);

        assertEquals(PermissionResult.ALLOW, fixture.resolver.permissions().check(user, "example.fly", QueryOptions.global()));
    }

    @Test
    void strictConflictReturnsUndefinedAndReportsBothCandidates() {
        var fixture = fixture(PermissionConflictResolution.STRICT, MetaFormatting.HIGHEST_WEIGHT);
        var user = conflictingUser(fixture, 50, 50);
        var explanation = fixture.resolver.permissions().explain(user, "example.fly", QueryOptions.global());

        assertEquals(PermissionResult.UNDEFINED, explanation.result());
        assertTrue(!explanation.winner().isPresent());
        assertEquals(2, explanation.candidates().stream().filter(candidate -> candidate.status() == CandidateStatus.CONFLICT).count());
        assertEquals(1, fixture.warnings.size());
        assertTrue(fixture.warnings.get(0).contains("example.fly"));
    }

    @Test
    void higherWeightStillWinsBeforeConflictPolicyIsApplied() {
        var fixture = fixture(PermissionConflictResolution.DENY_WINS, MetaFormatting.HIGHEST_WEIGHT);
        var user = conflictingUser(fixture, 51, 50);

        assertEquals(PermissionResult.ALLOW, fixture.resolver.permissions().check(user, "example.fly", QueryOptions.global()));
    }

    @Test
    void accumulatedMetaCombinesValuesInDescendingWeightOrder() {
        var fixture = fixture(PermissionConflictResolution.DENY_WINS, MetaFormatting.ACCUMULATED);

        fixture.groups.create("vip").toCompletableFuture().join();
        fixture.groups.create("admin").toCompletableFuture().join();

        fixture.groups.modify("vip", modifier -> {
            modifier.setWeight(50);
            modifier.setPrefix("[VIP]");
        }).toCompletableFuture().join();

        fixture.groups.modify("admin", modifier -> {
            modifier.setWeight(100);
            modifier.setPrefix("[Admin]");
        }).toCompletableFuture().join();

        var user = fixture.users.create(UUID.randomUUID(), "Alex").toCompletableFuture().join();

        user = fixture.users.modify(user, modifier -> {
            modifier.addGroup("vip");
            modifier.addGroup("admin");
        }).toCompletableFuture().join();

        assertEquals("[Admin][VIP]", fixture.resolver.options().prefix(user, QueryOptions.global()).get());
    }

    @Test
    void highestWeightMetaReturnsOnlyTheStrongestValue() {
        var fixture = fixture(PermissionConflictResolution.DENY_WINS, MetaFormatting.HIGHEST_WEIGHT);

        fixture.groups.create("vip").toCompletableFuture().join();
        fixture.groups.create("admin").toCompletableFuture().join();

        fixture.groups.modify("vip", modifier -> {
            modifier.setWeight(50);
            modifier.setPrefix("[VIP]");
        }).toCompletableFuture().join();

        fixture.groups.modify("admin", modifier -> {
            modifier.setWeight(100);
            modifier.setPrefix("[Admin]");
        }).toCompletableFuture().join();

        var user = fixture.users.create(UUID.randomUUID(), "Alex").toCompletableFuture().join();

        user = fixture.users.modify(user, modifier -> {
            modifier.addGroup("vip");
            modifier.addGroup("admin");
        }).toCompletableFuture().join();

        assertEquals("[Admin]", fixture.resolver.options().prefix(user, QueryOptions.global()).get());
    }

    private static User conflictingUser(Fixture fixture, int allowWeight, int denyWeight) {
        fixture.groups.create("allow").toCompletableFuture().join();
        fixture.groups.create("deny").toCompletableFuture().join();

        fixture.groups.modify("allow", modifier -> {
            modifier.setWeight(allowWeight);
            modifier.allowPermission("example.fly");
        }).toCompletableFuture().join();

        fixture.groups.modify("deny", modifier -> {
            modifier.setWeight(denyWeight);
            modifier.denyPermission("example.fly");
        }).toCompletableFuture().join();

        var user = fixture.users.create(UUID.randomUUID(), "Alex").toCompletableFuture().join();

        return fixture.users.modify(user, modifier -> {
            modifier.addGroup("allow");
            modifier.addGroup("deny");
        }).toCompletableFuture().join();
    }

    private static Fixture fixture(PermissionConflictResolution conflictResolution, MetaFormatting metaFormatting) {
        var store = new MemoryDataStore();
        store.open();

        var events = new EventBusImpl(error -> fail(error));
        var groups = new GroupManagerImpl(store, events, 10);
        var users = new UserManagerImpl(store, events);
        var ladders = new LadderManagerImpl(store, events);

        groups.attach(users, ladders);
        users.attachGroups(groups);
        ladders.attach(users, groups);

        var warnings = new ArrayList<String>();
        var resolver = new ResolverImpl(groups, 10, false, true, true, "default", conflictResolution, metaFormatting, warnings::add);

        return new Fixture(groups, users, resolver, warnings);
    }

    @Value
    @Accessors(fluent = true)
    private static class Fixture {
        GroupManagerImpl groups;
        UserManagerImpl users;
        ResolverImpl resolver;
        List<String> warnings;

        private Fixture(GroupManagerImpl groups, UserManagerImpl users, ResolverImpl resolver, List<String> warnings) {
            this.groups = groups;
            this.users = users;
            this.resolver = resolver;
            this.warnings = warnings;
        }
    }
}
