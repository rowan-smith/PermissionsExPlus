package ru.tehkode.permissions.backends.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.bukkit.PermissionsExConfig;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackendTest extends PEXTestBase {

    @TempDir
    Path tempDir;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        PermissionBackend.registerBackendAlias("file", FileBackend.class);
        
        // Set basedir to tempDir
        String baseDir = tempDir.toAbsolutePath().toString();
        yamlConfig.set("permissions.basedir", baseDir);
        yamlConfig.set("permissions.backend", "file");
        
        // Re-initialize config to pick up new basedir (it caches it)
        config = new PermissionsExConfig(yamlConfig, plugin);
        
        // Use reflection to set the private config field in manager if needed, 
        // but PermissionManager doesn't have a setConfig.
        // Let's check if PermissionManager has a constructor we can use or if we should just re-create it.
        manager = new PermissionManager(config, manager.getLogger(), nativeI);
    }

    @Test
    public void testFilePersistence() throws Exception {
        assertNotNull(manager.getBackend(), "Backend should not be null");
        assertTrue(manager.getBackend() instanceof FileBackend, "Backend should be an instance of FileBackend");
        
        PermissionGroup group = manager.getGroup("testGroup");
        group.addPermission("test.permission");
        group.setOption("test-option", "test-value");
        
        waitForExecutor();

        // Verify the file content if possible or just wait a bit more
        Thread.sleep(100);

        // Now create a new manager to load from the same file
        PermissionManager manager2 = new PermissionManager(config, manager.getLogger(), nativeI);
        
        PermissionGroup group2 = manager2.getGroup("testGroup");
        assertTrue(group2.getPermissions(null).contains("test.permission"), "Group permission should be preserved");
        assertEquals("test-value", group2.getOption("test-option", null), "Group option should be preserved");
    }

    @Test
    public void testWorldInheritancePersistence() throws Exception {
        manager.getBackend().setWorldInheritance("world_nether", Arrays.asList("world"));
        
        assertEquals(Collections.singletonList("world"), manager.getBackend().getWorldInheritance("world_nether"));

        // Create new manager to check persistence
        PermissionManager manager2 = new PermissionManager(config, manager.getLogger(), nativeI);
        assertEquals(Collections.singletonList("world"), manager2.getBackend().getWorldInheritance("world_nether"));
    }
}
