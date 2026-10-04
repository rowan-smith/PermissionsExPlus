package ru.tehkode.permissions.bukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.context.ContextSet;
import org.junit.jupiter.api.Test;

class GroupCommandsParityTest extends CommandAdapterTestSupport {

    @Test
    void createAndDeleteGroup() {
        assertTrue(run("pex group staff create"));
        assertExactMessage("Group \"staff\" created!");
        assertTrue(await(core.groups().find("staff")).isPresent());

        assertTrue(run("pex group staff delete"));
        assertExactMessage("Group \"staff\" removed!");
        assertTrue(await(core.groups().find("staff")).isEmpty());
    }

    @Test
    void createWithParentsSetsInheritance() {
        await(core.groups().create("member"));

        assertTrue(run("pex group staff create member"));
        var parents = await(core.groups().find("staff")).orElseThrow().parents();
        assertEquals(1, parents.size());
        assertEquals("member", parents.iterator().next().group());
    }

    @Test
    void groupsListShowsRegisteredGroups() {
        await(core.groups().create("staff"));
        await(core.groups().modify("staff", modifier -> modifier.setWeight(50)));

        assertTrue(run("pex groups"));
        assertMessageContains("Registered groups: ");
        assertMessageContains("staff");
        assertMessageContains("@50");
    }

    @Test
    void weightGetAndSet() {
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff weight 25"));
        assertExactMessage("Group \"staff\" has 25 calories.");
        assertEquals(25, await(core.groups().find("staff")).orElseThrow().weight().orElse(-1));

        assertTrue(run("pex group staff weight"));
        assertExactMessage("Group \"staff\" has 25 calories.");
    }

    @Test
    void addAndRemoveGroupPermission() {
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff add essentials.fly"));
        assertTrue(await(core.groups().find("staff")).orElseThrow().explicitlyAllows("essentials.fly", ContextSet.empty()));

        assertTrue(run("pex group staff remove essentials.fly"));
        assertTrue(await(core.groups().find("staff")).orElseThrow().explicitPermissions().stream()
                .noneMatch(node -> node.permission().equals("essentials.fly")));
    }

    @Test
    void worldScopedGroupPermission() {
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff add essentials.fly world_nether"));
        assertTrue(await(core.groups().find("staff")).orElseThrow()
                .explicitlyAllows("essentials.fly", world("world_nether")));
        assertFalse(await(core.groups().find("staff")).orElseThrow().explicitlyAllows("essentials.fly", ContextSet.empty()));
    }

    @Test
    void prefixSuffixAndOptionRoundTrip() {
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff prefix &c[S]"));
        assertTrue(run("pex group staff prefix"));
        assertExactMessage("staff's prefix is \"&c[S]\"");

        assertTrue(run("pex group staff suffix !"));
        assertTrue(run("pex group staff set build true"));
        assertTrue(run("pex group staff list"));
        assertMessageContains("build = \"true\"");
    }

    @Test
    void parentsSetAddAndRemove() {
        await(core.groups().create("member"));
        await(core.groups().create("vip"));
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff parents set member"));
        assertEquals("member", await(core.groups().find("staff")).orElseThrow().parents().iterator().next().group());

        assertTrue(run("pex group staff parents add vip"));
        assertEquals(2, await(core.groups().find("staff")).orElseThrow().parents().size());

        assertTrue(run("pex group staff parents remove member"));
        assertEquals(1, await(core.groups().find("staff")).orElseThrow().parents().size());
        assertEquals("vip", await(core.groups().find("staff")).orElseThrow().parents().iterator().next().group());
    }

    @Test
    void groupUsersListsCachedMembers() {
        createUser("Alex");
        await(core.groups().create("staff"));
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.addGroup("staff")));

        assertTrue(run("pex group staff users"));
        assertMessageContains("Group \"staff\"'s users (1):");
        assertMessageContains("Alex");
    }

    @Test
    void groupUserAddAndRemove() {
        createUser("Alex");
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff user add Alex"));
        assertTrue(requireUser("Alex").groups().stream().anyMatch(node -> node.group().equals("staff")));

        assertTrue(run("pex group staff user remove Alex"));
        assertTrue(requireUser("Alex").groups().stream().noneMatch(node -> node.group().equals("staff")));
    }

    @Test
    void defaultGroupReportsConfiguredFallback() {
        assertTrue(run("pex default group"));
        assertMessageContains("Default group: default");
    }

    @Test
    void checkGroupPermission() {
        await(core.groups().create("staff"));
        await(core.groups().modify("staff", modifier -> modifier.allowPermission("essentials.fly")));

        assertTrue(run("pex group staff check essentials.fly"));
        assertMessageContains("\"essentials.fly\" = allow");
    }

    @Test
    void duplicateCreateIsRejected() {
        await(core.groups().create("staff"));

        assertTrue(run("pex group staff create"));
        assertExactMessage("Group \"staff\" already exists.");
    }
}
