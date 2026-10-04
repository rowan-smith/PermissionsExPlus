package ru.tehkode.permissions.bukkit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class UtilityAndWorldCommandsParityTest extends CommandAdapterTestSupport {

    @Test
    void versionReportsAdapterIdentity() {
        assertTrue(run("pex version"));
        assertExactMessage("[PermissionsEx] version [" + adapter.getDescription().getVersion() + "]");
    }

    @Test
    void helpListsRegisteredLegacySyntax() {
        assertTrue(run("pex help 1 200"));
        assertMessageContains("PermissionsEx commands (page");
        assertMessageContains("user <user> add <permission> [world]");
        assertMessageContains("group <group> create [parents]");
        assertMessageContains("promote <user> [ladder]");
    }

    @Test
    void backendReportsMemoryStore() {
        assertTrue(run("pex backend"));
        assertMessageContains("Current backend:");
    }

    @Test
    void hierarchyListsGroupParents() {
        await(core.groups().create("member"));
        await(core.groups().create("staff"));
        await(core.groups().modify("staff", modifier -> modifier.addParent("member")));

        assertTrue(run("pex hierarchy"));
        assertMessageContains("User/Group inheritance hierarchy:");
        assertMessageContains("staff <- member");
    }

    @Test
    void worldsListsLoadedWorlds() {
        server.addSimpleWorld("world_nether");

        assertTrue(run("pex worlds"));
        assertMessageContains("Worlds on server: ");
        assertMessageContains("world_nether");
    }

    @Test
    void worldInfoForLoadedWorld() {
        server.addSimpleWorld("world_nether");

        assertTrue(run("pex world world_nether"));
        assertExactMessage("World \"world_nether\" inherits nothing.");
    }

    @Test
    void unsupportedLegacyUtilitiesExplainReplacement() {
        assertTrue(run("pex toggle debug"));
        assertMessageContains("Toggle debug via PermissionsExPlus config");

        assertTrue(run("pex convert uuid"));
        assertMessageContains("UUID conversion is handled by PermissionsExPlus");

        assertTrue(run("pex import file"));
        assertMessageContains("Import from legacy backends is not available");
    }
}
