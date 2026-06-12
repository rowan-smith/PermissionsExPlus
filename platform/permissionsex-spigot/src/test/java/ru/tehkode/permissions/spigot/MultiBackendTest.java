package ru.tehkode.permissions.spigot;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.spigot.bukkit.BukkitPEXBackendConfiguration;
import dev.rono.permissions.core.backends.MultiBackend;
import ru.tehkode.permissions.backends.PermissionBackend;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class MultiBackendTest extends PermissionsExSpigotTestBase {
    private PermissionBackend backend1;
    private PermissionBackend backend2;
    private MultiBackend multiBackend;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        manager = spy(manager);

        yamlConfig.set("permissions.backends.backend1.type", "memory");
        yamlConfig.set("permissions.backends.backend2.type", "memory");

        backend1 = manager.createBackend("backend1");
        backend2 = manager.createBackend("backend2");

        doReturn(backend1).when(manager).createBackend("backend1");
        doReturn(backend2).when(manager).createBackend("backend2");

        ConfigurationSection multiConfig = new MemoryConfiguration();
        multiConfig.set("backends", Arrays.asList("backend1", "backend2"));
        multiConfig.set("fallback.user", "backend2");
        multiConfig.set("fallback.group", "backend1");
        multiBackend = new MultiBackend(manager, new BukkitPEXBackendConfiguration(multiConfig));
    }

    @Test
    public void testUserPrioritization() {
        backend1.getUserData("user1").setPermissions(Collections.singletonList("perm1"), null);
        backend2.getUserData("user1").setPermissions(Collections.singletonList("perm2"), null);
        assertEquals(Collections.singletonList("perm1"), multiBackend.getUserData("user1").getPermissions(null));
    }

    @Test
    public void testGroupPrioritization() {
        backend1.getGroupData("group1").setPermissions(Collections.singletonList("perm1"), null);
        backend2.getGroupData("group1").setPermissions(Collections.singletonList("perm2"), null);
        assertEquals(Collections.singletonList("perm1"), multiBackend.getGroupData("group1").getPermissions(null));
    }

    @Test
    public void testFallbackAndInheritance() {
        assertFalse(multiBackend.hasUser("user3"));
        multiBackend.getUserData("user3").setPermissions(Collections.singletonList("fallback"), null);
        assertTrue(backend2.hasUser("user3"));

        backend2.setWorldInheritance("world", Arrays.asList("parent1", "parent2"));
        List<String> inheritance = multiBackend.getWorldInheritance("world");
        assertEquals(Arrays.asList("parent1", "parent2"), inheritance);
    }
}
