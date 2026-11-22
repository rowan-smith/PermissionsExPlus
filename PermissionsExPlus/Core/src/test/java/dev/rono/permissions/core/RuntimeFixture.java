package dev.rono.permissions.core;

import dev.rono.permissions.api.event.group.GroupCreatedEvent;
import dev.rono.permissions.api.event.group.GroupDeletedEvent;
import dev.rono.permissions.api.event.group.GroupModifiedEvent;
import dev.rono.permissions.api.event.user.UserModifiedEvent;
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
import dev.rono.permissions.core.store.DataStore;
import dev.rono.permissions.core.store.MemoryDataStore;

final class RuntimeFixture {
    final DataStore store;

    final EventBusImpl events = new EventBusImpl(error -> {
        throw new AssertionError(error);
    });

    final GroupManagerImpl groups;
    final UserManagerImpl users;
    final LadderManagerImpl ladders;
    final ResolutionSupport support;
    final PermissionEngine permissionEngine;
    final ResolverImpl resolvers;

    RuntimeFixture() {
        this(new MemoryDataStore());
    }

    /**
     * A runtime over existing storage, like a second server on a shared database.
     */
    RuntimeFixture(DataStore store) {
        this.store = store;

        groups = new GroupManagerImpl(store, events, 10);
        users = new UserManagerImpl(store, events);
        ladders = new LadderManagerImpl(store, events);

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

        events.subscribe(UserModifiedEvent.class, event -> permissionEngine.invalidateSubject("user:" + event.current().uniqueId()));
        events.subscribe(GroupModifiedEvent.class, event -> permissionEngine.invalidate());
        events.subscribe(GroupCreatedEvent.class, event -> permissionEngine.invalidate());
        events.subscribe(GroupDeletedEvent.class, event -> permissionEngine.invalidate());
    }

    static <T> T await(java.util.concurrent.CompletionStage<T> stage) {
        return stage.toCompletableFuture().join();
    }
}
