package ru.tehkode.permissions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class PermissionManagerTest extends PEXTestBase {

    @Test
    public void testUserRetrieval() {
        String uuid = UUID.randomUUID().toString();
        PermissionUser user = manager.getUser(uuid);
        assertNotNull(user);
        assertEquals(uuid, user.getIdentifier());

        PermissionUser sameUser = manager.getUser(uuid);
        assertSame(user, sameUser, "User objects should be cached and returned as same instance");
    }

    @Test
    public void testGroupRetrieval() {
        PermissionGroup group = manager.getGroup("NewGroup");
        assertNotNull(group);
        assertEquals("NewGroup", group.getIdentifier());

        PermissionGroup sameGroup = manager.getGroup("NewGroup");
        assertSame(group, sameGroup, "Group objects should be cached and returned as same instance");
    }

    @Test
    public void testDefaultGroup() {
        PermissionGroup defaultGroup = manager.getGroup("default");
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

    @Test
    public void testDefaultBackendConstantIsData() {
        assertEquals("data", ru.tehkode.permissions.backends.PermissionBackend.DEFAULT_BACKEND);
    }

    @Test
    public void testUserByNameAndUuidShareCache() {
        String uuid = UUID.randomUUID().toString();
        PermissionUser byId = manager.getUser(uuid);
        byId.setOption("name", "CachedName", null);

        PermissionUser again = manager.getUser(uuid);
        assertSame(byId, again);
        assertEquals("CachedName", again.getOption("name"));
    }

    @Test
    public void testBackendIsDataBridge() {
        assertNotNull(manager.getBackend());
        assertTrue(manager.getBackend() instanceof ru.tehkode.permissions.backends.data.PermissionBackend);
    }
}
