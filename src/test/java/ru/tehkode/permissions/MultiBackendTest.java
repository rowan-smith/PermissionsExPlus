package ru.tehkode.permissions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.backends.MultiBackend;
import ru.tehkode.permissions.backends.PermissionBackend;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MultiBackendTest extends PEXTestBase {
    private PermissionBackend backend1;
    private PermissionBackend backend2;
    private MultiBackend multiBackend;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        manager = spy(manager);

        // Configure backends in the yamlConfig used by PEXTestBase
        yamlConfig.set("permissions.backends.backend1.type", "memory");
        yamlConfig.set("permissions.backends.backend2.type", "memory");

        backend1 = manager.createBackend("backend1");
        backend2 = manager.createBackend("backend2");

        // Mock it to return our already created backends
        doReturn(backend1).when(manager).createBackend("backend1");
        doReturn(backend2).when(manager).createBackend("backend2");

        // Configure MultiBackend
        ConfigurationSection multiConfig = new MemoryConfiguration();
        multiConfig.set("backends", Arrays.asList("backend1", "backend2"));
        
        // Setup fallbacks
        multiConfig.set("fallback.user", "backend2");
        multiConfig.set("fallback.group", "backend1");

        multiBackend = new MultiBackend(manager, multiConfig);
    }

    @Test
    public void testUserPrioritization() {
        // User exists in both, backend1 should win
        backend1.getUserData("user1").setPermissions(Collections.singletonList("perm1"), null);
        backend2.getUserData("user1").setPermissions(Collections.singletonList("perm2"), null);

        assertTrue(multiBackend.hasUser("user1"));
        PermissionsUserData data = multiBackend.getUserData("user1");
        assertEquals(Collections.singletonList("perm1"), data.getPermissions(null));

        // User exists only in backend2
        backend2.getUserData("user2").setPermissions(Collections.singletonList("perm2"), null);
        assertTrue(multiBackend.hasUser("user2"));
        data = multiBackend.getUserData("user2");
        assertEquals(Collections.singletonList("perm2"), data.getPermissions(null));
    }

    @Test
    public void testGroupPrioritization() {
        // Group exists in both, backend1 should win
        backend1.getGroupData("group1").setPermissions(Collections.singletonList("perm1"), null);
        backend2.getGroupData("group1").setPermissions(Collections.singletonList("perm2"), null);

        assertTrue(multiBackend.hasGroup("group1"));
        PermissionsGroupData data = multiBackend.getGroupData("group1");
        assertEquals(Collections.singletonList("perm1"), data.getPermissions(null));

        // Group exists only in backend2
        backend2.getGroupData("group2").setPermissions(Collections.singletonList("perm2"), null);
        assertTrue(multiBackend.hasGroup("group2"));
        data = multiBackend.getGroupData("group2");
        assertEquals(Collections.singletonList("perm2"), data.getPermissions(null));
    }

    @Test
    public void testAggregation() {
        backend1.getUserData("user1").setPermissions(Collections.singletonList("perm1"), null);
        backend2.getUserData("user2").setPermissions(Collections.singletonList("perm2"), null);
        backend1.getGroupData("group1").setPermissions(Collections.singletonList("perm1"), null);
        backend2.getGroupData("group2").setPermissions(Collections.singletonList("perm2"), null);

        assertTrue(multiBackend.getUserIdentifiers().contains("user1"));
        assertTrue(multiBackend.getUserIdentifiers().contains("user2"));
        assertTrue(multiBackend.getGroupNames().contains("group1"));
        assertTrue(multiBackend.getGroupNames().contains("group2"));
    }

    @Test
    public void testFallback() {
        // user3 doesn't exist anywhere
        assertFalse(multiBackend.hasUser("user3"));
        PermissionsUserData data = multiBackend.getUserData("user3");
        // Should have returned data from backend2 (the fallback for users)
        data.setPermissions(Collections.singletonList("fallback-perm"), null);
        assertTrue(backend2.hasUser("user3"));
        assertFalse(backend1.hasUser("user3"));
    }

    @Test
    public void testWorldInheritance() {
        backend2.setWorldInheritance("world", Arrays.asList("parent1", "parent2"));
        
        List<String> inheritance = multiBackend.getWorldInheritance("world");
        assertEquals(Arrays.asList("parent1", "parent2"), inheritance);
        
        multiBackend.setWorldInheritance("world2", Collections.singletonList("parent3"));
        // Default fallback for world is the first backend (backend1) if not specified
        assertEquals(Collections.singletonList("parent3"), backend1.getWorldInheritance("world2"));
    }
}
