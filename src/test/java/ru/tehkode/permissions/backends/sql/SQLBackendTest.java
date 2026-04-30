package ru.tehkode.permissions.backends.sql;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SQLBackendTest extends PEXTestBase {
    private SQLBackend backend;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        ConfigurationSection sqlConfig = new MemoryConfiguration();
        // Use in-memory SQLite for testing with shared cache to keep data between connections
        sqlConfig.set("uri", "sqlite:file::memory:?cache=shared");
        
        backend = new SQLBackend(manager, sqlConfig);
    }

    @Test
    public void testTableDeployment() {
        // If the constructor finished, tables should be deployed
        assertTrue(backend.getGroupNames().contains("default"));
    }

    @Test
    public void testUserData() throws Exception {
        PermissionsUserData data = backend.getUserData("testUser");
        data.setPermissions(Arrays.asList("perm1", "perm2"), "world");
        
        // Wait for async saving (SQLBackend uses CachingUserData which uses executor)
        waitForExecutor();
        
        PermissionsUserData data2 = backend.getUserData("testUser");
        assertEquals(Arrays.asList("perm1", "perm2"), data2.getPermissions("world"));
    }

    @Test
    public void testGroupData() throws Exception {
        PermissionsGroupData data = backend.getGroupData("testGroup");
        data.setPermissions(Collections.singletonList("group-perm"), null);
        data.setParents(Collections.singletonList("default"), null);
        
        waitForExecutor();
        
        PermissionsGroupData data2 = backend.getGroupData("testGroup");
        assertEquals(Collections.singletonList("group-perm"), data2.getPermissions(null));
        assertEquals(Collections.singletonList("default"), data2.getParents(null));
    }

    @Test
    public void testWorldInheritance() {
        backend.setWorldInheritance("world1", Arrays.asList("parent1", "parent2"));
        
        List<String> inheritance = backend.getWorldInheritance("world1");
        assertEquals(Arrays.asList("parent1", "parent2"), inheritance);
    }
}
