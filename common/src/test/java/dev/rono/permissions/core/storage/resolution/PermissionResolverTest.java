package dev.rono.permissions.core.storage.resolution;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.core.storage.model.Group;
import dev.rono.permissions.core.storage.model.GroupInheritance;
import dev.rono.permissions.core.storage.model.GroupOptions;
import dev.rono.permissions.core.storage.model.GroupPermission;
import dev.rono.permissions.core.storage.model.Ladder;
import dev.rono.permissions.core.storage.model.LadderGroup;
import dev.rono.permissions.core.storage.model.User;
import dev.rono.permissions.core.storage.model.UserGroup;
import dev.rono.permissions.core.storage.model.UserOptions;
import dev.rono.permissions.core.storage.model.UserPermission;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionResolverTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

    @Test
    void userPermissionOverridesGroupAllow() {
        User user = new User(USER_ID, "player", NOW, NOW,
                List.of(new UserGroup(USER_ID, 1, null)),
                List.of(new UserPermission(USER_ID, "test.perm", false, null, null)),
                new UserOptions(null, null));
        Group group = new Group(1, "vip", 10, false,
                List.of(new GroupPermission(1, "test.perm", true, null, null)),
                List.of(), new GroupOptions(null, null));

        EffectiveUser effective = PermissionResolver.resolve(
                user, Map.of(1, group), List.of(), PermissionContext.global(), NOW);

        assertFalse(effective.hasPermission("test.perm"));
        assertEquals("user", effective.resolve("test.perm", null).getSource());
    }

    @Test
    void ladderPositionIncreasesGroupPriority() {
        User user = new User(USER_ID, "player", NOW, NOW,
                List.of(new UserGroup(USER_ID, 1, null), new UserGroup(USER_ID, 2, null)),
                List.of(), new UserOptions(null, null));
        Group low = new Group(1, "a", 0, false,
                List.of(new GroupPermission(1, "ranked", false, null, null)),
                List.of(), new GroupOptions(null, null));
        Group high = new Group(2, "b", 0, false,
                List.of(new GroupPermission(2, "ranked", true, null, null)),
                List.of(), new GroupOptions(null, null));
        Ladder ladder = new Ladder(1, "staff", List.of(
                new LadderGroup(1, 1, 1),
                new LadderGroup(1, 2, 5)));

        EffectiveUser effective = PermissionResolver.resolve(
                user, Map.of(1, low, 2, high), List.of(ladder), PermissionContext.global(), NOW);

        assertTrue(effective.hasPermission("ranked"));
        assertEquals("group", effective.resolve("ranked", null).getSource());
    }

    @Test
    void inheritedPermissionsApplyWithDepthPenalty() {
        User user = new User(USER_ID, "player", NOW, NOW,
                List.of(new UserGroup(USER_ID, 1, null)),
                List.of(), new UserOptions(null, null));
        Group child = new Group(1, "child", 5, false,
                List.of(), List.of(new GroupInheritance(1, 2)), new GroupOptions(null, null));
        Group parent = new Group(2, "parent", 1, false,
                List.of(new GroupPermission(2, "inherit.me", true, null, null)),
                List.of(), new GroupOptions(null, null));

        EffectiveUser effective = PermissionResolver.resolve(
                user, Map.of(1, child, 2, parent), List.of(), PermissionContext.global(), NOW);

        assertTrue(effective.hasPermission("inherit.me"));
        assertEquals("inheritance", effective.resolve("inherit.me", null).getSource());
    }

    @Test
    void expiredPermissionsAreIgnored() {
        User user = new User(USER_ID, "player", NOW, NOW,
                List.of(),
                List.of(new UserPermission(USER_ID, "gone", true, null, NOW.minusSeconds(1))),
                new UserOptions(null, null));

        EffectiveUser effective = PermissionResolver.resolve(
                user, Map.of(), List.of(), PermissionContext.global(), NOW);

        assertNull(effective.resolve("gone", null));
    }
}
