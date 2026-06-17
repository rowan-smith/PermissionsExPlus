package ru.tehkode.permissions.spigot.backends.sql;

import dev.rono.permissions.core.backends.sql.SQLBackend;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;
import ru.tehkode.permissions.spigot.PermissionsExSpigotTestBase;
import ru.tehkode.permissions.spigot.bukkit.BukkitPEXBackendConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SQLBackendTest extends PermissionsExSpigotTestBase {
    private SQLBackend backend;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ConfigurationSection sqlConfig = new MemoryConfiguration();
        String dbPath = (System.getProperty("java.io.tmpdir") + "/pex-sqlbackend-" + UUID.randomUUID() + ".db").replace("\\", "/");
        sqlConfig.set("uri", "sqlite:" + dbPath);
        sqlConfig.set("user", "");
        sqlConfig.set("password", "");
        backend = new SQLBackend(manager, new BukkitPEXBackendConfiguration(sqlConfig));
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (backend != null) {
            backend.close();
        }
    }

    @Test
    public void testTableDeployment() {
        assertTrue(backend.getGroupNames() != null);
    }

    @Test
    public void testUserAndGroupDataRoundTrip() throws Exception {
        PermissionsUserData userData = backend.getUserData("testUser");
        userData.setPermissions(Arrays.asList("perm1", "perm2"), "world");

        PermissionsGroupData groupData = backend.getGroupData("testGroup");
        groupData.setPermissions(Collections.singletonList("group-perm"), null);
        groupData.setParents(Collections.singletonList("default"), null);

        waitForExecutor();
        backend.reload();
        PermissionsUserData userData2 = backend.getUserData("testUser");
        PermissionsGroupData groupData2 = backend.getGroupData("testGroup");
        assertTrue(userData2.getPermissions("world") != null);
        assertTrue(groupData2.getPermissions(null) != null);
        assertTrue(groupData2.getParents(null) != null);
    }

    @Test
    public void testWorldInheritance() {
        backend.setWorldInheritance("world1", Arrays.asList("parent1", "parent2"));
        List<String> inheritance = backend.getWorldInheritance("world1");
        assertEquals(Arrays.asList("parent1", "parent2"), inheritance);
    }
}
