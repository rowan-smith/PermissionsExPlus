package ru.tehkode.permissions.bukkit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

/**
 * Exact chat-output contracts against classic PEX 1.x wording.
 * If a message drifts, these tests fail with the full captured transcript.
 */
class LegacyMessageContractTest extends CommandAdapterTestSupport {

    @Test
    void missingUserUsesClassicDoesntExistWording() {
        assertTrue(run("pex user Nobody add essentials.fly"));
        assertExactMessage("User \"Nobody\" doesn't exist.");
    }

    @Test
    void missingGroupUsesClassicDoesntExistWording() {
        assertTrue(run("pex group ghost list"));
        assertExactMessage("Group \"ghost\" doesn't exist.");
    }

    @Test
    void usersListUsesRegisteredUsersHeader() {
        createUser("Alex");

        assertTrue(run("pex users"));
        assertMessagesStartWith("Currently registered users: ");
        assertMessageContains("Alex");
    }

    @Test
    void userPermissionAddAndRemoveMatchClassicConfirmations() {
        createUser("Alex");

        assertTrue(run("pex user Alex add essentials.fly"));
        assertExactMessage("Permission \"essentials.fly\" added!");

        assertTrue(run("pex user Alex remove essentials.fly"));
        assertExactMessage("Permission \"essentials.fly\" removed!");
    }

    @Test
    void userPermissionAddInformsOnlinePlayerWithClassicText() {
        PlayerMock player = server.addPlayer("Alex");
        await(core.users().create(player.getUniqueId(), "Alex"));
        drainPlayer(player);

        assertTrue(run("pex user Alex add essentials.fly"));
        assertExactMessage("Permission \"essentials.fly\" added!");
        org.junit.jupiter.api.Assertions.assertEquals(
                "[PermissionsEx] Your permissions have been changed!",
                org.bukkit.ChatColor.stripColor(player.nextMessage()));
    }

    @Test
    void userPrefixGetAndSetMatchClassicWording() {
        createUser("Alex");

        assertTrue(run("pex user Alex prefix &a[A]"));
        assertExactMessage("Alex's prefix has been set to \"&a[A]\"");

        assertTrue(run("pex user Alex prefix"));
        assertExactMessage("Alex's prefix is \"&a[A]\"");
    }

    @Test
    void userPrefixWorldScopedUsesInWorldClause() {
        createUser("Alex");

        assertTrue(run("pex user Alex prefix &a[A] world_nether"));
        assertExactMessage("Alex's prefix (in world \"world_nether\") has been set to \"&a[A]\"");

        assertTrue(run("pex user Alex prefix world_nether"));
        // without newprefix, second arg is treated as world by syntax matcher when only one optional filled —
        // our syntax is [newprefix] [world]; a single token binds to newprefix. Set via API then get with world.
        await(core.users().modify(requireUser("Alex").uniqueId(),
                modifier -> modifier.setPrefix("&b[N]", world("world_nether"))));

        // get with explicit empty newprefix isn't possible; list via option resolve through command without world:
        assertTrue(run("pex user Alex suffix &7* world_nether"));
        assertExactMessage("Alex's suffix (in world \"world_nether\") has been set to \"&7*\"");
    }

    @Test
    void userSuffixGetAndSetMatchClassicWording() {
        createUser("Alex");

        assertTrue(run("pex user Alex suffix &7*"));
        assertExactMessage("Alex's suffix has been set to \"&7*\"");

        assertTrue(run("pex user Alex suffix"));
        assertExactMessage("Alex's suffix is \"&7*\"");
    }

    @Test
    void userCheckUsesHasDoesntHaveWording() {
        createUser("Alex");
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.allowPermission("essentials.fly")));

        assertTrue(run("pex user Alex check essentials.fly"));
        assertExactMessage("Player \"Alex\" has \"essentials.fly\"");

        assertTrue(run("pex user Alex check essentials.god"));
        assertExactMessage("Player \"Alex\" doesn't have \"essentials.god\"");
    }

    @Test
    void userGetOptionUsesPlayerAtWorldWording() {
        createUser("Alex");
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.setOption("build", "true")));

        assertTrue(run("pex user Alex get build"));
        assertExactMessage("Player \"Alex\" @ null option \"build\" = \"true\"");
    }

    @Test
    void userDeleteUsesClassicRemovedWording() {
        createUser("Alex");

        assertTrue(run("pex user Alex delete"));
        assertExactMessage("User \"Alex\" removed!");
    }

    @Test
    void userTimedPermissionUsesClassicAddedWording() {
        createUser("Alex");

        assertTrue(run("pex user Alex timed add essentials.fly 60"));
        assertExactMessage("Timed permission \"essentials.fly\" added!");
    }

    @Test
    void userSetOptionUsesClassicSetWording() {
        createUser("Alex");

        assertTrue(run("pex user Alex set build true"));
        assertExactMessage("Option \"build\" set!");
    }

    @Test
    void userGroupListUsesAtWorldWording() {
        createUser("Alex");
        await(core.groups().create("staff"));
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.addGroup("staff")));

        assertTrue(run("pex user Alex group list"));
        assertExactMessages(
                "User \"Alex\" @null currently in:",
                "  staff");
    }

    @Test
    void userGroupAddSetRemoveMatchClassicWording() {
        createUser("Alex");
        await(core.groups().create("staff"));
        await(core.groups().create("member"));

        assertTrue(run("pex user Alex group add staff"));
        assertExactMessage("User \"Alex\" added to group \"staff\"!");

        assertTrue(run("pex user Alex group set member"));
        assertExactMessage("User groups set!");

        assertTrue(run("pex user Alex group remove member"));
        assertExactMessage("User \"Alex\" removed from group \"member\"!");
    }

    @Test
    void userInfoUsesIsAMemberOfWording() {
        createUser("Alex");
        await(core.groups().create("staff"));
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> {
            modifier.addGroup("staff");
            modifier.allowPermission("essentials.fly");
            modifier.setOption("build", "true");
        }));

        assertTrue(run("pex user Alex list"));
        assertExactMessages(
                "'Alex' is a member of:",
                "  staff",
                "Alex's permissions:",
                "  essentials.fly",
                "Alex's options:",
                "  build = \"true\"");
    }

    @Test
    void userSuperpermsOfflineUsesClassicOfflineWording() {
        createUser("Alex");

        assertTrue(run("pex user Alex superperms"));
        assertExactMessage("Player not found (offline?)");
    }

    @Test
    void groupCreateDeleteAndDuplicateMatchClassicWording() {
        assertTrue(run("pex group staff create"));
        assertExactMessage("Group \"staff\" created!");

        assertTrue(run("pex group staff create"));
        assertExactMessage("Group \"staff\" already exists.");

        assertTrue(run("pex group staff delete"));
        assertExactMessage("Group \"staff\" removed!");
    }

    @Test
    void groupWeightUsesCaloriesWording() {
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff weight 25"));
        assertExactMessage("Group \"staff\" has 25 calories.");

        assertTrue(run("pex group staff weight"));
        assertExactMessage("Group \"staff\" has 25 calories.");
    }

    @Test
    void groupPrefixSuffixMatchClassicWording() {
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff prefix &c[S]"));
        assertExactMessage("staff's prefix has been set to \"&c[S]\"");

        assertTrue(run("pex group staff prefix"));
        assertExactMessage("staff's prefix is \"&c[S]\"");

        assertTrue(run("pex group staff suffix !"));
        assertExactMessage("staff's suffix has been set to \"!\"");
    }

    @Test
    void groupPermissionAndOptionMessagesMatchClassic() {
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff add essentials.fly"));
        assertExactMessage("Permission \"essentials.fly\" added to group \"staff\"!");

        assertTrue(run("pex group staff set build true"));
        assertExactMessage("Option \"build\" set!");

        assertTrue(run("pex group staff remove essentials.fly"));
        assertExactMessage("Permission \"essentials.fly\" removed from group \"staff\"!");

        assertTrue(run("pex group staff timed add essentials.fly 30"));
        assertExactMessage("Timed permission added!");
    }

    @Test
    void groupParentsMessagesMatchClassic() {
        await(core.groups().create("member"));
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff parents list"));
        assertExactMessage("Group \"staff\" has no parents.");

        assertTrue(run("pex group staff parents set member"));
        assertExactMessage("Group staff inheritance updated!");

        assertTrue(run("pex group staff parents list"));
        assertExactMessages(
                "Group staff parents:",
                "  member");

        assertTrue(run("pex group staff parents remove member"));
        assertExactMessage("Group \"staff\" inheritance updated!");
    }

    @Test
    void groupListPermissionsUsesClassicHeaders() {
        await(core.groups().create("staff"));
        await(core.groups().modify("staff", modifier -> {
            modifier.allowPermission("essentials.fly");
            modifier.setOption("build", "true");
        }));

        assertTrue(run("pex group staff list"));
        assertExactMessages(
                "Group \"staff\"'s permissions:",
                "  essentials.fly",
                "Group \"staff\"'s Options: ",
                "  build = \"true\"");
    }

    @Test
    void groupUsersListingUsesClassicCountHeader() {
        createUser("Alex");
        await(core.groups().create("staff"));
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.addGroup("staff")));

        assertTrue(run("pex group staff users"));
        assertExactMessages(
                "Group \"staff\"'s users (1):",
                "   Alex");
    }

    @Test
    void groupUserAddRemoveMatchClassicWording() {
        createUser("Alex");
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff user add Alex"));
        assertExactMessage("User Alex added to staff !");

        assertTrue(run("pex group staff user remove Alex"));
        assertExactMessage("User Alex removed from staff !");
    }

    @Test
    void groupsListUsesRegisteredGroupsHeader() {
        await(core.groups().create("staff"));
        await(core.groups().modify("staff", modifier -> modifier.setWeight(50)));

        assertTrue(run("pex groups"));
        assertMessagesStartWith("Registered groups: ");
        assertMessageContains("staff");
        assertMessageContains("@50");
    }

    @Test
    void promoteAndDemoteMatchClassicWording() {
        await(core.groups().create("member"));
        await(core.groups().create("vip"));
        await(core.ladders().create("default"));
        await(core.ladders().modify("default", modifier -> modifier.setGroups(List.of("member", "vip"))));
        createUser("Alex");
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.addGroup("member")));

        assertTrue(run("pex promote Alex"));
        assertExactMessage("User Alex promoted to vip group");

        assertTrue(run("pex demote Alex"));
        assertExactMessage("User Alex demoted to member group");
    }

    @Test
    void promoteAtTopUsesPromotionErrorStatus() {
        await(core.groups().create("staff"));
        await(core.ladders().create("default"));
        await(core.ladders().modify("default", modifier -> modifier.setGroups(List.of("staff"))));
        createUser("Alex");
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.addGroup("staff")));

        assertTrue(run("pex promote Alex"));
        assertExactMessage("Promotion error: ALREADY_TOP");
    }

    @Test
    void rankGetAndSetMatchClassicLadderWording() {
        await(core.groups().create("vip"));

        assertTrue(run("pex group vip rank"));
        assertExactMessage("Group vip is unranked");

        assertTrue(run("pex group vip rank 0 default"));
        assertExactMessage("Group vip rank is 0 (ladder = default)");

        assertTrue(run("pex group vip rank"));
        assertExactMessage("Group vip rank is 0 (ladder = default)");
    }

    @Test
    void reloadAndVersionMatchClassicWording() {
        assertTrue(run("pex reload"));
        assertExactMessage("Permissions reloaded");

        assertTrue(run("pex version"));
        assertExactMessage("[PermissionsEx] version [" + adapter.getDescription().getVersion() + "]");
    }

    @Test
    void backendAndHierarchyMatchClassicHeaders() {
        await(core.groups().create("member"));
        await(core.groups().create("staff"));
        await(core.groups().modify("staff", modifier -> modifier.addParent("member")));

        assertTrue(run("pex backend"));
        assertMessagesStartWith("Current backend: ");

        assertTrue(run("pex hierarchy"));
        assertMessagesStartWith("User/Group inheritance hierarchy:");
        assertMessageContains("staff <- member");
    }

    @Test
    void helpUsesClassicPermissionsExCommandsHeader() {
        assertTrue(run("pex help 1 5"));
        assertMessagesStartWith("PermissionsEx commands (page 1/");
        assertMessageContains("/pex");
    }

    @Test
    void worldsMatchClassicListingAndMissingWorldWording() {
        server.addSimpleWorld("world_nether");

        assertTrue(run("pex worlds"));
        assertMessagesStartWith("Worlds on server: ");
        assertMessageContains("world_nether");

        assertTrue(run("pex world missing_world"));
        assertExactMessage("Specified world \"missing_world\" not found.");

        assertTrue(run("pex world world_nether"));
        assertExactMessage("World \"world_nether\" inherits nothing.");
    }

    private void drainPlayer(PlayerMock player) {
        while (player.nextMessage() != null) {
            // discard join noise
        }
    }
}
