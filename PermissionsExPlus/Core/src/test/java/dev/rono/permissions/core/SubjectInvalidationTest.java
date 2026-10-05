package dev.rono.permissions.core;

import static dev.rono.permissions.core.RuntimeFixture.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.core.engine.CachedPermissionEngine;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SubjectInvalidationTest {
    @Test
    void userInvalidationDoesNotBumpUnrelatedUserCacheEntries() {
        var runtime = new RuntimeFixture();
        var cached = (CachedPermissionEngine) runtime.permissionEngine;

        var first = await(runtime.users.create(UUID.randomUUID(), "First"));
        var second = await(runtime.users.create(UUID.randomUUID(), "Second"));

        first = await(runtime.users.modify(first, modifier -> modifier.allowPermission("keep.cache")));
        second = await(runtime.users.modify(second, modifier -> modifier.allowPermission("keep.cache")));

        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(first, "keep.cache", QueryOptions.global()));
        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(second, "keep.cache", QueryOptions.global()));

        long hitsBefore = cached.hitCount();
        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(second, "keep.cache", QueryOptions.global()));
        assertTrue(cached.hitCount() > hitsBefore);

        first = await(runtime.users.modify(first, modifier -> modifier.denyPermission("keep.cache")));

        // Second user's decision should still be served from cache after subject-scoped invalidation.
        long hitsAfterMutation = cached.hitCount();
        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(second, "keep.cache", QueryOptions.global()));
        assertTrue(cached.hitCount() > hitsAfterMutation);

        assertEquals(PermissionResult.DENY, runtime.resolvers.permissions().check(first, "keep.cache", QueryOptions.global()));
    }
}
