package ru.tehkode.permissions.bukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;

class PromotionCommandsParityTest extends CommandAdapterTestSupport {

    @Test
    void rankSetAndGetOnLadder() {
        await(core.groups().create("member"));
        await(core.groups().create("vip"));
        await(core.groups().create("staff"));

        assertTrue(run("pex group member rank 0 default"));
        assertTrue(run("pex group vip rank 1 default"));
        assertTrue(run("pex group staff rank 2 default"));

        var ladder = await(core.ladders().find("default")).orElseThrow();
        assertEquals(List.of("member", "vip", "staff"), ladder.groups());

        assertTrue(run("pex group vip rank"));
        assertExactMessage("Group vip rank is 1 (ladder = default)");
    }

    @Test
    void promoteAndDemoteMoveUserAlongLadder() {
        await(core.groups().create("member"));
        await(core.groups().create("vip"));
        await(core.groups().create("staff"));
        await(core.ladders().create("default"));
        await(core.ladders().modify("default", modifier -> modifier.setGroups(List.of("member", "vip", "staff"))));

        createUser("Alex");
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.addGroup("member")));

        assertTrue(run("pex promote Alex"));
        assertExactMessage("User Alex promoted to vip group");
        assertTrue(requireUser("Alex").groups().stream().anyMatch(node -> node.group().equals("vip")));
        assertTrue(requireUser("Alex").groups().stream().noneMatch(node -> node.group().equals("member")));

        assertTrue(run("demote Alex"));
        assertExactMessage("User Alex demoted to member group");
        assertTrue(requireUser("Alex").groups().stream().anyMatch(node -> node.group().equals("member")));
    }

    @Test
    void promoteAliasUsesPromoteCommand() {
        await(core.groups().create("member"));
        await(core.groups().create("vip"));
        await(core.ladders().create("default"));
        await(core.ladders().modify("default", modifier -> modifier.setGroups(List.of("member", "vip"))));

        createUser("Alex");
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.addGroup("member")));

        assertTrue(run("promote Alex"));
        assertTrue(requireUser("Alex").groups().stream().anyMatch(node -> node.group().equals("vip")));
    }

    @Test
    void promoteAtTopReportsStatus() {
        await(core.groups().create("staff"));
        await(core.ladders().create("default"));
        await(core.ladders().modify("default", modifier -> modifier.setGroups(List.of("staff"))));

        createUser("Alex");
        await(core.users().modify(requireUser("Alex").uniqueId(), modifier -> modifier.addGroup("staff")));

        assertTrue(run("pex promote Alex"));
        assertExactMessage("Promotion error: ALREADY_TOP");
    }
}
