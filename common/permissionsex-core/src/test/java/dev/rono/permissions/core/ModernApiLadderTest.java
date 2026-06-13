package dev.rono.permissions.core;

import dev.rono.permissions.api.RankingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Rank ladder promotion, demotion, and ranking queries. */
class ModernApiLadderTest extends ModernApiTestSupport {

    @Test
    void promoteAndDemoteMoveUserBetweenRankedGroups() throws RankingException {
        var mod = api().getGroupManager().createGroup("ladder-mod");
        mod.setRank(2, "test-ladder");
        mod.save();
        var admin = api().getGroupManager().createGroup("ladder-admin");
        admin.setRank(1, "test-ladder");
        admin.save();

        var user = api().getUserManager().createUser("ladder-move-user");
        user.addGroup("ladder-mod", null);
        user.save();

        var ladders = api().getLadderManager();
        assertEquals("ladder-admin", ladders.promote(user, "test-ladder").getName());
        assertTrue(ladders.isRanked(user, "test-ladder"));
        assertEquals(1, ladders.rank(user, "test-ladder"));

        assertEquals("ladder-mod", ladders.demote(user, "test-ladder").getName());
        assertEquals(2, ladders.rank(user, "test-ladder"));
    }

    @Test
    void promoteAtTopThrowsRankingException() throws RankingException {
        var top = api().getGroupManager().createGroup("ladder-top");
        top.setRank(1, "top-ladder");
        top.save();
        var user = api().getUserManager().createUser("ladder-top-user");
        user.addGroup("ladder-top", null);
        user.save();

        assertThrows(RankingException.class, () -> api().getLadderManager().promote(user, "top-ladder"));
    }

    @Test
    void groupRankMetadataReadable() {
        var group = api().getGroupManager().createGroup("rank-meta-group");
        group.setRank(3, "meta-ladder");
        group.save();

        assertEquals(3, group.rank());
        assertEquals("meta-ladder", group.rankLadder());
    }
}
