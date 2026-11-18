package dev.rono.permissions.core;

import lombok.var;

import static dev.rono.permissions.core.RuntimeFixture.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.context.ContextKeys;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.config.MetaFormatting;
import dev.rono.permissions.core.context.WorldInheritanceImpl;
import dev.rono.permissions.core.engine.PermissionEngines;
import dev.rono.permissions.core.resolver.ResolverImpl;
import dev.rono.permissions.core.util.Immutables;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * With world_nether inheriting from world, data set for world applies in
 * world_nether, as in PermissionsEx, for every kind of query and both engines.
 */
class WorldInheritanceResolutionTest {
    private static final QueryOptions NETHER = options("world_nether");
    private static final QueryOptions END = options("world_the_end");

    private RuntimeFixture runtime;
    private User user;

    @BeforeEach
    void setUp() {
        runtime = new RuntimeFixture();
        runtime.resolvers.expandContexts(WorldInheritanceImpl.inMemory(Immutables.mapOf("world_nether", Immutables.listOf("world")))::expand);

        var world = ContextSet.builder().add(ContextKeys.WORLD, "world").build();

        await(runtime.groups.create("builder"));
        await(runtime.groups.modify("builder", modifier -> modifier.allowPermission("world.build")));

        user = await(runtime.users.create(UUID.randomUUID(), "Alex"));
        user = await(runtime.users.modify(user, modifier -> {
            modifier.allowPermission("world.fly", world);
            modifier.setPrefix("[Overworld]", world);
            modifier.addGroup("builder", world);
        }));
    }

    @Test
    void permissionsSetForAnInheritedWorldApply() {
        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(user, "world.fly", NETHER));
        assertEquals(PermissionResult.UNDEFINED, runtime.resolvers.permissions().check(user, "world.fly", END));
    }

    @Test
    void groupMembershipsAndOptionsFollowTheInheritance() {
        assertTrue(runtime.resolvers.inheritance().inherits(user, "builder", NETHER));
        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(user, "world.build", NETHER));
        assertEquals(Optional.of("[Overworld]"), runtime.resolvers.options().resolve(user, "prefix", NETHER));
        assertEquals(PermissionResult.UNDEFINED, runtime.resolvers.permissions().check(user, "world.build", END));
    }

    @Test
    void theUncachedEngineAgrees() {
        var resolver = new ResolverImpl(runtime.groups, runtime.support, MetaFormatting.HIGHEST_WEIGHT, PermissionEngines.create(runtime.support));
        resolver.expandContexts(WorldInheritanceImpl.inMemory(Immutables.mapOf("world_nether", Immutables.listOf("world")))::expand);

        assertEquals(PermissionResult.ALLOW, resolver.permissions().check(user, "world.fly", NETHER));
        assertEquals(PermissionResult.ALLOW, resolver.permissions().check(user, "world.build", NETHER));
        assertEquals(PermissionResult.UNDEFINED, resolver.permissions().check(user, "world.fly", END));
    }

    private static QueryOptions options(String world) {
        return QueryOptions.builder().contexts(ContextSet.builder().add(ContextKeys.WORLD, world).build()).build();
    }
}
