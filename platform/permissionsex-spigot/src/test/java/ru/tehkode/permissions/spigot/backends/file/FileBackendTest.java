package ru.tehkode.permissions.spigot.backends.file;

import dev.rono.permissions.core.DefaultPermissionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.spigot.PermissionsExSpigotTestBase;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackendTest extends PermissionsExSpigotTestBase {
    @TempDir
    Path tempDir;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        yamlConfig.set("permissions.basedir", tempDir.toAbsolutePath().toString());
        yamlConfig.set("permissions.backend", "file");
        manager = new DefaultPermissionManager(config, manager.getLogger(), platformRuntime);
    }

    @Test
    public void testFilePersistence() throws Exception {
        PermissionGroup group = manager.getGroup("testGroup");
        group.addPermission("test.permission");
        group.setOption("test-option", "test-value");
        manager.getBackend().close();

        PermissionManager manager2 = new DefaultPermissionManager(config, manager.getLogger(), platformRuntime);
        PermissionGroup group2 = manager2.getGroup("testGroup");
        assertTrue(group2.getPermissions(null).contains("test.permission"));
        assertEquals("test-value", group2.getOption("test-option", null));
    }

    @Test
    public void testWorldInheritancePersistence() throws Exception {
        manager.getBackend().setWorldInheritance("world_nether", Arrays.asList("world"));
        assertEquals(Collections.singletonList("world"), manager.getBackend().getWorldInheritance("world_nether"));
        manager.getBackend().close();

        PermissionManager manager2 = new DefaultPermissionManager(config, manager.getLogger(), platformRuntime);
        assertEquals(Collections.singletonList("world"), manager2.getBackend().getWorldInheritance("world_nether"));
    }
}
