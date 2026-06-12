package ru.tehkode.permissions;

import org.junit.jupiter.api.Test;
import java.util.Collections;
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
    public void testRanking() throws Exception {
        PermissionGroup group1 = manager.getGroup("Group1");
        PermissionGroup group2 = manager.getGroup("Group2");
        
        group1.setRank(100);
        group1.setRankLadder("default");
        group2.setRank(50);
        group2.setRankLadder("default");
        
        assertEquals(100, group1.getRank());
        assertEquals(50, group2.getRank());
        
        PermissionUser user = manager.getUser("TestUser");
        user.addGroup(group1);
        
        assertTrue(user.inGroup(group1));
        
        // Force manager to load all groups into its cache and backend
        manager.getGroups();
        
        // Promotion logic
        user.promote(null, "default");
        
        assertTrue(user.inGroup(group2), "User should be promoted to Group2");
        assertFalse(user.inGroup(group1), "User should no longer be in Group1");
    }
}