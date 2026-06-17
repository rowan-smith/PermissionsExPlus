package dev.rono.permissions.core.commands;

import dev.rono.permissions.core.config.CommandFramework;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PexCloudCommandIntegrationTest extends PEXTestBase {

    @Test
    void classicGameServerUserPermissionAddAndRemove() {
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.CLASSIC, CoreCloudPlatform.GAME_SERVER);

        List<String> addMessages = harness.execute("pex user Rono add test.perm");
        assertEquals("Permission \"test.perm\" added!", addMessages.getFirst());
        assertTrue(manager.getUser("Rono").has("test.perm", "survival"));

        List<String> removeMessages = harness.execute("pex user Rono remove test.perm");
        assertEquals("Permission \"test.perm\" removed!", removeMessages.getFirst());
        assertFalse(manager.getUser("Rono").has("test.perm", "survival"));
    }

    @Test
    void classicProxyServerSubtreeUsesServerSyntax() {
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.CLASSIC, CoreCloudPlatform.PROXY);
        harness.setDefaultWorld("lobby");

        assertTrue(harness.execute("pex servers").getFirst().startsWith("Worlds on server:"));
        assertTrue(harness.execute("pex server lobby").stream().anyMatch(line -> line.contains("inherits")));
    }

    @Test
    void classicGameServerWorldSubtreeUsesWorldSyntax() {
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.CLASSIC, CoreCloudPlatform.GAME_SERVER);

        assertTrue(harness.execute("pex worlds").getFirst().startsWith("Worlds on server:"));
        assertTrue(harness.execute("pex world world").stream().anyMatch(line -> line.contains("inherits")));
    }

    @Test
    void modernUserPermissionAddCheckAndList() {
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.GAME_SERVER);

        assertEquals(
                "Permission \"modern.perm\" added!",
                harness.execute("pex user Rono permissions add modern.perm --world survival").getFirst());
        assertTrue(manager.getUser("Rono").has("modern.perm", "survival"));

        assertTrue(harness.execute("pex user Rono permissions check modern.perm --world survival")
                .getFirst()
                .contains("true"));

        assertTrue(harness.execute("pex user Rono permissions list --world survival").stream()
                .anyMatch(line -> line.contains("modern.perm")));
    }

    @Test
    void modernProxyPrefersServerFlagOverWorldFlag() {
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.PROXY);
        harness.setDefaultWorld("lobby");

        harness.execute("pex user Rono permissions add proxy.perm --world ignored --server proxy-a");
        PermissionUser user = manager.getUser("Rono");
        assertTrue(user.has("proxy.perm", "proxy-a"));
        assertFalse(user.has("proxy.perm", "ignored"));
    }

    @Test
    void modernGameServerPrefersWorldFlagOverServerFlag() {
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.GAME_SERVER);

        harness.execute("pex user Rono permissions add game.perm --server ignored --world survival");
        PermissionUser user = manager.getUser("Rono");
        assertTrue(user.has("game.perm", "survival"));
        assertFalse(user.has("game.perm", "ignored"));
    }

    @Test
    void modernGroupPermissionManagement() {
        manager.getGroup("staff");
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.GAME_SERVER);

        assertEquals(
                "Permission \"staff.node\" added to group \"staff\"!",
                harness.execute("pex group staff permissions add staff.node").getFirst());
        assertTrue(manager.getGroup("staff").getOwnPermissions("survival").contains("staff.node"));
    }

    @Test
    void classicAndModernSuggestKnownUsers() {
        manager.getUser("Rono");
        PexCloudCommandTestSupport.TestHarness classic =
                PexCloudCommandTestSupport.install(manager, CommandFramework.CLASSIC, CoreCloudPlatform.GAME_SERVER);
        PexCloudCommandTestSupport.TestHarness modern =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.GAME_SERVER);

        assertFalse(classic.suggest("pex user ").isEmpty());
        assertFalse(modern.suggest("pex user ").isEmpty());
    }

    @Test
    void modernUserPermissionTraceReportsMatchingNode() {
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.GAME_SERVER);
        harness.execute("pex user Rono permissions add trace.perm --world survival");

        assertTrue(harness.execute("pex user Rono permissions trace trace.perm --world survival").stream()
                .anyMatch(line -> line.contains("Matching expression") || line.contains("Effective result")));
    }

    @Test
    void classicAndModernReloadCommandsWork() {
        PexCloudCommandTestSupport.TestHarness classic =
                PexCloudCommandTestSupport.install(manager, CommandFramework.CLASSIC, CoreCloudPlatform.GAME_SERVER);
        PexCloudCommandTestSupport.TestHarness modern =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.GAME_SERVER);

        assertEquals("Permissions reloaded", classic.execute("pex reload").getFirst());
        assertEquals("Permissions reloaded", modern.execute("pex reload").getFirst());
    }

    @Test
    void promoteAndDemoteCommandsWorkInClassicTree() {
        manager.getGroup("admin").setRankLadder("default");
        manager.getGroup("admin").setRank(1);
        manager.getGroup("mod").setRankLadder("default");
        manager.getGroup("mod").setRank(2);
        manager.getUser("Rono").setParents(List.of(manager.getGroup("mod")));

        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.CLASSIC, CoreCloudPlatform.GAME_SERVER);

        assertEquals("User Rono promoted to admin group", harness.execute("promote Rono").getFirst());
        assertEquals("User Rono demoted to mod group", harness.execute("demote Rono").getFirst());
    }

    @Test
    void modernLadderPromoteAndDemoteCommandsWork() {
        manager.getGroup("admin").setRankLadder("staff");
        manager.getGroup("admin").setRank(1);
        manager.getGroup("mod").setRankLadder("staff");
        manager.getGroup("mod").setRank(2);
        manager.getUser("Rono").setParents(List.of(manager.getGroup("mod")));

        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.GAME_SERVER);

        assertEquals(
                "User Rono promoted to admin group",
                harness.execute("pex ladder staff promote Rono").getFirst());
        assertEquals(
                "User Rono demoted to mod group",
                harness.execute("pex ladder staff demote Rono").getFirst());
    }

    @Test
    void modernFrameworkDoesNotRegisterTopLevelPromoteCommands() {
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.GAME_SERVER);

        assertTrue(harness.suggest("pex ").stream().noneMatch(part -> part.equals("promote")));
        assertTrue(harness.suggest("pex ").stream().noneMatch(part -> part.equals("demote")));
        assertTrue(harness.suggest("").stream().noneMatch(part -> part.equals("promote")));
    }

    @Test
    void modernLadderManagementCommandsWork() {
        manager.getGroup("helper").setRankLadder("staff");
        manager.getGroup("helper").setRank(2);

        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.GAME_SERVER);

        assertFalse(harness.execute("pex ladders").isEmpty());
        assertFalse(harness.execute("pex ladder staff info").isEmpty());
        assertEquals(
                "Group \"helper\" added to ladder \"staff\" at rank 3",
                harness.execute("pex ladder staff groups add helper").getFirst());
    }
}
