package ru.tehkode.permissions;

import org.junit.jupiter.api.Test;
import java.util.Collection;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class PermissionManagerTest extends PEXTestBase {

    @Test
    public void testUserRetrieval() {
        String uuid = UUID.randomUUID().toString();
        PermissionUser user = manager.getUser(uuid);
        assertNotNull(user);
        assertEquals(uuid, user.getIdentifier());
        
        PermissionUser sameUser = manager.getUser(uuid);
        assertSame(user, sameUser, "PexUser objects should be cached and returned as same instance");
    }

    @Test
    public void testGroupRetrieval() {
        PermissionGroup group = manager.getGroup("NewGroup");
        assertNotNull(group);
        assertEquals("NewGroup", group.getIdentifier());
        
        PermissionGroup sameGroup = manager.getGroup("NewGroup");
        assertSame(group, sameGroup, "PexGroup objects should be cached and returned as same instance");
    }

    @Test
    public void testDefaultGroup() {
        PermissionGroup defaultGroup = manager.getGroup("default");
        defaultGroup.setDefault(true, null);
        
        // Force manager to see the group in its group list
        manager.getGroups(); 
        
        assertTrue(defaultGroup.isDefault(null));
        
        Collection<PermissionGroup> defaults = manager.getDefaultGroups(null);
        boolean found = false;
        for (PermissionGroup g : defaults) {
            if (g.getName().equals("default")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Default group should be found in default groups list");
    }

    @Test
    public void testGetGroups() {
        manager.getGroup("Group1");
        manager.getGroup("Group2");
        
        Collection<PermissionGroup> groups = manager.getGroupList();
        assertTrue(groups.size() >= 2, "Should have at least 2 groups, but had " + groups.size());
    }
}