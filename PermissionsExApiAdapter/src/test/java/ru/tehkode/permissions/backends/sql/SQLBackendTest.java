package ru.tehkode.permissions.backends.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;

public class SQLBackendTest extends PEXTestBase {
    @TempDir
    Path tempDir;

    private SQLBackend backend;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        ConfigurationSection sqlConfig = new MemoryConfiguration();
        // Prefer an on-disk temp database over shared-memory SQLite to avoid
        // SQLITE_LOCKED_SHAREDCACHE races between the pool and async writers.
        String database = tempDir.resolve("permissions.db").toAbsolutePath().toString().replace('\\', '/');
        sqlConfig.set("uri", "sqlite:" + database);

        backend = new SQLBackend(manager, sqlConfig);
        backend.awaitPendingTasks();
        // Deploy writes the default group asynchronously; clear name caches so
        // subsequent reads observe the persisted entity.
        backend.reload();
    }

    @AfterEach
    public void tearDownBackend() throws Exception {
        if (backend != null) {
            backend.close();
        }
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

        backend.awaitPendingTasks();
        PermissionsUserData data2 = backend.getUserData("testUser");
        assertEquals(Arrays.asList("perm1", "perm2"), data2.getPermissions("world"));
    }

    @Test
    public void testGroupData() throws Exception {
        PermissionsGroupData data = backend.getGroupData("testGroup");
        data.setPermissions(Collections.singletonList("group-perm"), null);
        data.setParents(Collections.singletonList("default"), null);

        backend.awaitPendingTasks();
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
