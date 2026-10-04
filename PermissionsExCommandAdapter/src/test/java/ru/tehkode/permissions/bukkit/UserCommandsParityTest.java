package ru.tehkode.permissions.bukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.permission.PermissionValue;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class UserCommandsParityTest extends CommandAdapterTestSupport {

    @Test
    void usersListShowsCachedUsers() {
        createUser("Alex");
        createUser("Blake");

        assertTrue(run("pex users"));
        assertMessageContains("Currently registered users: ");
        assertMessageContains("Alex");
        assertMessageContains("Blake");
    }

    @Test
    void addAndRemoveUserPermissionMutatesStoredNodes() {
        createUser("Alex");

        assertTrue(run("pex user Alex add essentials.fly"));
        assertExactMessage("Permission \"essentials.fly\" added!");
        assertTrue(requireUser("Alex").explicitlyAllows("essentials.fly", ContextSet.empty()));

        assertTrue(run("pex user Alex remove essentials.fly"));
        assertExactMessage("Permission \"essentials.fly\" removed!");
        assertTrue(requireUser("Alex").explicitPermissions().stream()
                .noneMatch(node -> node.permission().equals("essentials.fly")));
    }

    @Test
    void denyPermissionIsStoredAsDenyValue() {
        createUser("Alex");

        assertTrue(run("pex user Alex add -essentials.fly"));
        var node = requireUser("Alex").explicitPermissions().stream()
                .filter(entry -> entry.permission().equals("essentials.fly"))
                .findFirst()
                .orElseThrow();

        assertEquals(PermissionValue.DENY, node.value());
    }

    @Test
    void worldScopedPermissionUsesWorldContext() {
        createUser("Alex");

        assertTrue(run("pex user Alex add essentials.fly world_nether"));
        assertTrue(requireUser("Alex").explicitlyAllows("essentials.fly", world("world_nether")));
        assertFalse(requireUser("Alex").explicitlyAllows("essentials.fly", ContextSet.empty()));
    }

    @Test
    void prefixAndSuffixRoundTrip() {
        createUser("Alex");

        assertTrue(run("pex user Alex prefix &a[A]"));
        assertExactMessage("Alex's prefix has been set to \"&a[A]\"");

        assertTrue(run("pex user Alex prefix"));
        assertExactMessage("Alex's prefix is \"&a[A]\"");

        assertTrue(run("pex user Alex suffix &7*"));
        assertTrue(run("pex user Alex suffix"));
        assertExactMessage("Alex's suffix is \"&7*\"");
    }

    @Test
    void optionGetAndSet() {
        createUser("Alex");

        assertTrue(run("pex user Alex set build true"));
        assertExactMessage("Option \"build\" set!");

        assertTrue(run("pex user Alex get build"));
        assertExactMessage("Player \"Alex\" @ null option \"build\" = \"true\"");
    }

    @Test
    void checkResolvesEffectivePermission() {
        createUser("Alex");
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.allowPermission("essentials.fly")));

        assertTrue(run("pex user Alex check essentials.fly"));
        assertExactMessage("Player \"Alex\" has \"essentials.fly\"");

        assertEquals(PermissionResult.ALLOW, core.resolvers().permissions()
                .check(requireUser("Alex"), "essentials.fly", ApiWrapper.query(null)));
    }

    @Test
    void groupMembershipAddSetAndRemove() {
        createUser("Alex");
        await(core.groups().create("staff"));
        await(core.groups().create("member"));

        assertTrue(run("pex user Alex group add staff"));
        assertTrue(requireUser("Alex").groups().stream().anyMatch(node -> node.group().equals("staff")));

        assertTrue(run("pex user Alex group set member"));
        var groups = requireUser("Alex").groups();
        assertEquals(1, groups.size());
        assertEquals("member", groups.iterator().next().group());

        assertTrue(run("pex user Alex group remove member"));
        assertTrue(requireUser("Alex").groups().isEmpty());
    }

    @Test
    void groupListShowsMembership() {
        createUser("Alex");
        await(core.groups().create("staff"));
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.addGroup("staff")));

        assertTrue(run("pex user Alex group list"));
        assertMessageContains("User \"Alex\" @null currently in:");
        assertMessageContains("staff");
    }

    @Test
    void userInfoListsPermissionsOptionsAndGroups() {
        createUser("Alex");
        await(core.groups().create("staff"));
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> {
            modifier.addGroup("staff");
            modifier.allowPermission("essentials.fly");
            modifier.setOption("build", "true");
        }));

        assertTrue(run("pex user Alex list"));
        assertMessageContains("'Alex' is a member of:");
        assertMessageContains("staff");
        assertMessageContains("essentials.fly");
        assertMessageContains("build = \"true\"");
    }

    @Test
    void deleteRemovesUser() {
        UUID id = createUser("Alex").uniqueId();

        assertTrue(run("pex user Alex delete"));
        assertExactMessage("User \"Alex\" removed!");
        assertTrue(await(core.users().find(id)).isEmpty());
    }

    @Test
    void timedPermissionIsAddedWithExpiry() {
        createUser("Alex");

        assertTrue(run("pex user Alex timed add essentials.fly 60"));
        assertExactMessage("Timed permission \"essentials.fly\" added!");
        assertTrue(requireUser("Alex").explicitPermissions().stream()
                .anyMatch(node -> node.permission().equals("essentials.fly") && node.expiry().isPresent()));
    }

    @Test
    void missingUserReportsError() {
        assertTrue(run("pex user Nobody add essentials.fly"));
        assertExactMessage("User \"Nobody\" doesn't exist.");
    }

    @Test
    void playerWithoutPermissionIsDenied() {
        createUser("Alex");
        PlayerMock player = server.addPlayer("helper");
        player.setOp(false);

        assertTrue(run(player, "pex user Alex add essentials.fly"));
        assertEquals("Sorry, you don't have enough permissions.", ChatColor.stripColor(player.nextMessage()));
        assertTrue(requireUser("Alex").explicitPermissions().isEmpty());
    }
}
