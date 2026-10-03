package dev.rono.permissions.core;

import dev.rono.permissions.core.config.MetaFormatting;
import dev.rono.permissions.core.config.PermissionConflictResolution;
import dev.rono.permissions.core.engine.PermissionEngine;
import dev.rono.permissions.core.engine.PermissionEngines;
import dev.rono.permissions.core.event.EventBusImpl;
import dev.rono.permissions.core.manager.GroupManagerImpl;
import dev.rono.permissions.core.manager.LadderManagerImpl;
import dev.rono.permissions.core.manager.UserManagerImpl;
import dev.rono.permissions.core.resolver.ResolutionSupport;
import dev.rono.permissions.core.resolver.ResolverImpl;
import dev.rono.permissions.core.store.MemoryDataStore;

final class RuntimeFixture {
    final MemoryDataStore store = new MemoryDataStore();

    final EventBusImpl events = new EventBusImpl(error -> {
        throw new AssertionError(error);
    });

    final GroupManagerImpl groups = new GroupManagerImpl(store, events, 10);
    final UserManagerImpl users = new UserManagerImpl(store, events);
    final LadderManagerImpl ladders = new LadderManagerImpl(store, events);
    final ResolutionSupport support;
    final PermissionEngine permissionEngine;
    final ResolverImpl resolvers;

    RuntimeFixture() {
        store.open();

        groups.attach(users, ladders);
        users.attachGroups(groups);
        ladders.attach(users, groups);

        support = new ResolutionSupport(
                groups,
                10,
                false,
                true,
                true,
                "default",
                PermissionConflictResolution.DENY_WINS,
                warning -> {
                    throw new AssertionError(warning);
                });

        permissionEngine = PermissionEngines.createCached(support);
        resolvers = new ResolverImpl(
                groups,
                support,
                MetaFormatting.HIGHEST_WEIGHT,
                permissionEngine);

        events.subscribe(dev.rono.permissions.api.event.user.UserModifiedEvent.class, event -> permissionEngine.invalidate());
        events.subscribe(dev.rono.permissions.api.event.group.GroupModifiedEvent.class, event -> permissionEngine.invalidate());
    }

    static <T> T await(java.util.concurrent.CompletionStage<T> stage) {
        return stage.toCompletableFuture().join();
    }
}
