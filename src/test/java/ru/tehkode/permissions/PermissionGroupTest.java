package ru.tehkode.permissions;

import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.exceptions.RankingException;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PermissionGroupTest extends PEXTestBase {

    @Test
    public void testGroupPermissions() {
        PermissionGroup group = manager.getGroup("TestGroup");
        group.addPermission("test.permission");

        assertTrue(group.has("test.permission"), "Group should have test.permission");
    }

    @Test
    public void testGroupInheritance() {
        PermissionGroup parent = manager.getGroup("ParentGroup");
        PermissionGroup child = manager.getGroup("ChildGroup");

        parent.addPermission("parent.permission");
        child.setParents(Collections.singletonList(parent));

        assertTrue(child.has("parent.permission"), "Child group should inherit parent.permission");
    }

    @Test
    public void testDefaultWeight() {
        PermissionGroup group = manager.getGroup("TestGroup");
        assertEquals(0, group.getWeight(), "Default weight should be 0");
    }

    @Test
    public void testSetWeight() {
        PermissionGroup group = manager.getGroup("TestGroup");
        group.setWeight(50);
        assertEquals(50, group.getWeight());
    }

    @Test
    public void testWeightSorting() {
        PermissionGroup low = manager.getGroup("LowWeight");
        PermissionGroup high = manager.getGroup("HighWeight");
        low.setWeight(10);
        high.setWeight(100);
        assertTrue(high.compareTo(low) > 0, "High weight group should sort after low weight group");
        assertTrue(low.compareTo(high) < 0, "Low weight group should sort before high weight group");
    }

    @Test
    public void testDefaultRank() {
        PermissionGroup group = manager.getGroup("TestGroup");
        assertEquals(0, group.getRank(), "Default rank should be 0 (unranked)");
    }

    @Test
    public void testIsRanked() {
        PermissionGroup group = manager.getGroup("TestGroup");
        assertFalse(group.isRanked());
        group.setRank(10);
        assertTrue(group.isRanked());
    }

    @Test
    public void testSetRank() {
        PermissionGroup group = manager.getGroup("TestGroup");
        group.setRank(5);
        assertEquals(5, group.getRank());
        group.setRank(0);
        assertEquals(0, group.getRank(), "Setting rank to 0 should unrank the group");
        assertFalse(group.isRanked());
    }

    @Test
    public void testDefaultRankLadder() {
        PermissionGroup group = manager.getGroup("TestGroup");
        assertEquals("default", group.getRankLadder(), "Default rank ladder should be 'default'");
    }

    @Test
    public void testSetRankLadder() {
        PermissionGroup group = manager.getGroup("TestGroup");
        group.setRankLadder("vip");
        assertEquals("vip", group.getRankLadder());
    }

    @Test
    public void testPromote() throws RankingException {
        PermissionGroup lower = manager.getGroup("Member");
        PermissionGroup higher = manager.getGroup("VIP");
        lower.setRank(100);
        lower.setRankLadder("default");
        higher.setRank(50);
        higher.setRankLadder("default");

        PermissionUser user = manager.getUser("TestUser");
        user.addGroup(lower);

        assertTrue(user.inGroup(lower));
        assertEquals(100, user.getRank("default"));

        PermissionGroup promoted = user.promote(null, "default");
        assertEquals(higher, promoted, "User should be promoted to the VIP group");
        assertTrue(user.inGroup(higher));
        assertFalse(user.inGroup(lower));
    }

    @Test
    public void testDemote() throws RankingException {
        PermissionGroup higher = manager.getGroup("VIP");
        PermissionGroup lower = manager.getGroup("Member");
        higher.setRank(50);
        higher.setRankLadder("default");
        lower.setRank(100);
        lower.setRankLadder("default");

        PermissionUser user = manager.getUser("TestUser");
        user.addGroup(higher);

        assertTrue(user.inGroup(higher));

        PermissionGroup demoted = user.demote(null, "default");
        assertEquals(lower, demoted, "User should be demoted to the Member group");
        assertTrue(user.inGroup(lower));
        assertFalse(user.inGroup(higher));
    }

    @Test
    public void testPromoteAtTopThrows() {
        PermissionGroup top = manager.getGroup("Top");
        top.setRank(1);
        top.setRankLadder("default");

        PermissionUser user = manager.getUser("TestUser");
        user.addGroup(top);

        assertThrows(RankingException.class, () -> user.promote(null, "default"));
    }

    @Test
    public void testDemoteAtBottomThrows() {
        PermissionGroup bottom = manager.getGroup("Bottom");
        bottom.setRank(100);
        bottom.setRankLadder("default");

        PermissionUser user = manager.getUser("TestUser");
        user.addGroup(bottom);

        assertThrows(RankingException.class, () -> user.demote(null, "default"));
    }

    @Test
    public void testPromoteWithPromoter() throws RankingException {
        PermissionGroup modRank = manager.getGroup("Mod");
        PermissionGroup adminRank = manager.getGroup("Admin");
        PermissionGroup headAdminRank = manager.getGroup("HeadAdmin");
        modRank.setRank(50);
        modRank.setRankLadder("default");
        adminRank.setRank(10);
        adminRank.setRankLadder("default");
        headAdminRank.setRank(5);
        headAdminRank.setRankLadder("default");

        PermissionUser promoter = manager.getUser("Promoter");
        promoter.addGroup(headAdminRank);

        PermissionUser target = manager.getUser("Target");
        target.addGroup(modRank);

        PermissionGroup result = target.promote(promoter, "default");
        assertEquals(adminRank, result, "Promoter with higher rank should be able to promote");
    }

    @Test
    public void testPromoteWithInsufficientPromoterRankThrows() {
        PermissionGroup modRank = manager.getGroup("Mod");
        PermissionGroup adminRank = manager.getGroup("Admin");
        modRank.setRank(50);
        modRank.setRankLadder("default");
        adminRank.setRank(10);
        adminRank.setRankLadder("default");

        PermissionUser promoter = manager.getUser("Promoter");
        promoter.addGroup(modRank);

        PermissionUser target = manager.getUser("Target");
        target.addGroup(modRank);

        assertThrows(RankingException.class, () -> target.promote(promoter, "default"),
                "Promoter at same rank as target should not be able to promote");
    }

    @Test
    public void testUnrankedUserThrowsOnPromote() {
        PermissionUser user = manager.getUser("TestUser");

        assertThrows(RankingException.class, () -> user.promote(null, "default"),
                "User not in any ladder should throw");
    }

    @Test
    public void testUnrankedUserThrowsOnDemote() {
        PermissionUser user = manager.getUser("TestUser");

        assertThrows(RankingException.class, () -> user.demote(null, "default"),
                "User not in any ladder should throw");
    }

    @Test
    public void testMultipleLaddersIndependent() throws RankingException {
        PermissionGroup vip = manager.getGroup("VIP");
        PermissionGroup vipPlus = manager.getGroup("VIP+");
        vip.setRank(100);
        vip.setRankLadder("vip");
        vipPlus.setRank(50);
        vipPlus.setRankLadder("vip");

        PermissionGroup member = manager.getGroup("Member");
        PermissionGroup memberPlus = manager.getGroup("Member+");
        member.setRank(200);
        member.setRankLadder("member");
        memberPlus.setRank(100);
        memberPlus.setRankLadder("member");

        PermissionUser user = manager.getUser("TestUser");
        user.addGroup(vip);
        user.addGroup(member);

        Map<String, PermissionGroup> ladders = user.getRankLadders();
        assertEquals(2, ladders.size(), "User should be in two ladders");
        assertEquals(vip, ladders.get("vip"));
        assertEquals(member, ladders.get("member"));

        PermissionGroup promoted = user.promote(null, "vip");
        assertEquals(vipPlus, promoted);
        assertTrue(user.inGroup(vipPlus));
        assertFalse(user.inGroup(vip));
    }

    @Test
    public void testPromoteThreeGroups() throws RankingException {
        PermissionGroup low = manager.getGroup("Low");
        PermissionGroup mid = manager.getGroup("Mid");
        PermissionGroup high = manager.getGroup("High");
        low.setRank(100);
        low.setRankLadder("default");
        mid.setRank(50);
        mid.setRankLadder("default");
        high.setRank(10);
        high.setRankLadder("default");

        PermissionUser user = manager.getUser("TestUser");
        user.addGroup(low);

        PermissionGroup promoted1 = user.promote(null, "default");
        assertEquals(mid, promoted1, "First promote should go to Mid");

        PermissionGroup promoted2 = user.promote(null, "default");
        assertEquals(high, promoted2, "Second promote should go to High");

        assertThrows(RankingException.class, () -> user.promote(null, "default"),
                "Third promote at top should throw");
    }
}