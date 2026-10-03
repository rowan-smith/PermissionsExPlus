package dev.rono.permissions.core;

import static dev.rono.permissions.core.RuntimeFixture.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.core.engine.CachedPermissionEngine;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PermissionEngineParityTest {
    @Test
    void checkMatchesExplainForCoreScenarios() {
        var runtime = new RuntimeFixture();

        await(runtime.groups.create("staff"));
        await(runtime.groups.modify("staff", modifier -> modifier.allowPermission("chat.send")));

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));

        user = await(runtime.users.modify(user, modifier -> {
            modifier.addGroup("staff");
            modifier.denyPermission("chat.send");
            modifier.allowPermission("world.build");
            modifier.denyPermission("world.*");
        }));

        assertEquals(
                runtime.resolvers.permissions().explain(user, "chat.send", QueryOptions.global()).result(),
                runtime.resolvers.permissions().check(user, "chat.send", QueryOptions.global()));
        assertEquals(PermissionResult.DENY, runtime.resolvers.permissions().check(user, "chat.send", QueryOptions.global()));

        assertEquals(
                runtime.resolvers.permissions().explain(user, "world.build", QueryOptions.global()).result(),
                runtime.resolvers.permissions().check(user, "world.build", QueryOptions.global()));
        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(user, "world.build", QueryOptions.global()));

        assertEquals(
                runtime.resolvers.permissions().explain(user, "missing.perm", QueryOptions.global()).result(),
                runtime.resolvers.permissions().check(user, "missing.perm", QueryOptions.global()));
        assertEquals(PermissionResult.UNDEFINED, runtime.resolvers.permissions().check(user, "missing.perm", QueryOptions.global()));
    }

    @Test
    void checkMatchesExplainForContextualAndTimedPermissions() throws Exception {
        var runtime = new RuntimeFixture();

        var survival = ContextSet.builder().add("world", "survival").build();
        var creative = ContextSet.builder().add("world", "creative").build();

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));

        user = await(runtime.users.modify(user, modifier -> {
            modifier.allowPermission("kit.use", survival);
            modifier.allowTimedPermission("temporary.use", Duration.ofMillis(150));
        }));

        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(user, "kit.use", survival));
        assertEquals(PermissionResult.UNDEFINED, runtime.resolvers.permissions().check(user, "kit.use", creative));
        assertEquals(
                runtime.resolvers.permissions().explain(user, "kit.use", creative).result(),
                runtime.resolvers.permissions().check(user, "kit.use", creative));

        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(user, "temporary.use", QueryOptions.global()));
        assertEquals(
                runtime.resolvers.permissions().explain(user, "temporary.use", QueryOptions.global()).result(),
                runtime.resolvers.permissions().check(user, "temporary.use", QueryOptions.global()));

        Thread.sleep(200);

        assertEquals(PermissionResult.UNDEFINED, runtime.resolvers.permissions().check(user, "temporary.use", QueryOptions.global()));
        assertEquals(
                runtime.resolvers.permissions().explain(user, "temporary.use", QueryOptions.global()).result(),
                runtime.resolvers.permissions().check(user, "temporary.use", QueryOptions.global()));
    }

    @Test
    void decisionCacheServesHitsUntilInvalidation() {
        var runtime = new RuntimeFixture();
        var cached = (CachedPermissionEngine) runtime.permissionEngine;

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));
        user = await(runtime.users.modify(user, modifier -> modifier.allowPermission("cache.test")));

        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(user, "cache.test", QueryOptions.global()));
        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(user, "cache.test", QueryOptions.global()));
        assertTrue(cached.hitCount() >= 1);

        var before = cached.revision();
        user = await(runtime.users.modify(user, modifier -> modifier.denyPermission("cache.test")));
        assertTrue(cached.revision() > before);

        assertEquals(PermissionResult.DENY, runtime.resolvers.permissions().check(user, "cache.test", QueryOptions.global()));
    }

    @Test
    void contextOrderDoesNotCreateSeparateCacheEntries() {
        var runtime = new RuntimeFixture();
        var cached = (CachedPermissionEngine) runtime.permissionEngine;

        var first = ContextSet.builder().add("world", "survival").add("server", "main").build();
        var second = ContextSet.builder().add("server", "main").add("world", "survival").build();

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));
        user = await(runtime.users.modify(user, modifier -> modifier.allowPermission("order.test", first)));

        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(user, "order.test", first));
        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(user, "order.test", second));
        assertTrue(cached.hitCount() >= 1);
    }
}
