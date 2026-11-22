package dev.rono.permissions.core;

import lombok.var;

import static dev.rono.permissions.core.RuntimeFixture.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.event.group.GroupDeletedEvent;
import dev.rono.permissions.api.event.user.UserDeletedEvent;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.user.User;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Two runtimes over one store stand in for two servers on a shared database;
 * {@code refreshFromStorage} is what a Redis invalidation runs on the receiver.
 */
class RemoteRefreshTest {
    private final RuntimeFixture first = new RuntimeFixture();
    private final RuntimeFixture second = new RuntimeFixture(first.store);

    @Test
    void groupChangeOnAnotherServerIsSeenAfterRefresh() {
        await(first.groups.create("vip"));
        await(first.groups.modify("vip", modifier -> modifier.allowPermission("vip.fly")));
        var id = UUID.randomUUID();
        await(first.users.create(id, "Alex"));
        await(first.users.modify(id, modifier -> modifier.addGroup(ParentNode.builder().group("vip").build())));

        second.groups.loadAll();
        assertEquals(PermissionResult.ALLOW, check(second, id, "vip.fly"));

        // The permission is revoked on the first server, which never had this
        // player cached in the scenario the review describes.
        await(first.groups.modify("vip", modifier -> modifier.denyPermission("vip.fly")));
        second.groups.refreshFromStorage("vip");

        assertEquals(PermissionResult.DENY, check(second, id, "vip.fly"));
    }

    @Test
    void userChangeOnAnotherServerClearsCachedDecisions() {
        var id = UUID.randomUUID();
        await(first.users.create(id, "Alex"));
        await(first.users.modify(id, modifier -> modifier.allowPermission("example.use")));

        second.groups.loadAll();
        assertEquals(PermissionResult.ALLOW, check(second, id, "example.use"));

        await(first.users.modify(id, modifier -> modifier.denyPermission("example.use")));
        second.users.refreshFromStorage(id);

        assertEquals(PermissionResult.DENY, check(second, id, "example.use"));
    }

    @Test
    void groupDeletedOnAnotherServerLeavesTheCache() {
        await(first.groups.create("vip"));
        second.groups.loadAll();
        var deleted = new ArrayList<String>();
        second.events.subscribe(GroupDeletedEvent.class, event -> deleted.add(event.group().name()));

        await(first.groups.delete("vip"));
        second.groups.refreshFromStorage("vip");

        assertFalse(second.groups.cache().get("vip").isPresent());
        assertEquals(1, deleted.size());
    }

    @Test
    void groupCreatedOnAnotherServerIsLoaded() {
        second.groups.loadAll();

        await(first.groups.create("staff"));
        second.groups.refreshFromStorage("staff");

        assertTrue(second.groups.cache().get("staff").isPresent());
    }

    @Test
    void userDeletedOnAnotherServerLeavesTheCache() {
        var id = UUID.randomUUID();
        await(first.users.create(id, "Alex"));
        await(second.users.find(id));
        var deleted = new ArrayList<UUID>();
        second.events.subscribe(UserDeletedEvent.class, event -> deleted.add(event.user().uniqueId()));

        await(first.users.delete(id));
        second.users.refreshFromStorage(id);

        assertFalse(second.users.cache().isCached(id));
        assertEquals(1, deleted.size());
    }

    @Test
    void usersNotCachedHereAreLeftForTheNextLoad() {
        var id = UUID.randomUUID();
        await(first.users.create(id, "Alex"));

        second.users.refreshFromStorage(id);

        assertFalse(second.users.cache().isCached(id));
    }

    private static PermissionResult check(RuntimeFixture runtime, UUID id, String permission) {
        User user = await(runtime.users.find(id)).get();

        return runtime.resolvers.permissions().check(user, permission, QueryOptions.global());
    }
}
