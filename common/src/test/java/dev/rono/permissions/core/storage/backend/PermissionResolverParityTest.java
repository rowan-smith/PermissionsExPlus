package dev.rono.permissions.core.storage.backend;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.storage.LocalSqlRepository;
import dev.rono.permissions.core.storage.resolution.EffectiveUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ensures runtime {@code has()} checks agree with {@link dev.rono.permissions.core.storage.resolution.PermissionResolver}.
 */
class PermissionResolverParityTest extends ru.tehkode.permissions.PEXTestBase {

    private LocalSqlRepository repository;
    private LocalSqlBackend backend;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        repository = LocalSqlRepository.inMemory("parity-" + System.nanoTime());
        repository.deploySchema();
        repository.ensureSchemaLatest();
        backend = new LocalSqlBackend(manager, repository, java.io.File.createTempFile("unused", ".yml"));
        assertInstanceOf(DefaultPermissionManager.class, manager);
        ((DefaultPermissionManager) manager).adoptBackend(backend);
    }

    @AfterEach
    void tearDownBackend() throws PermissionBackendException {
        if (backend != null) {
            backend.close();
        }
        if (repository != null) {
            repository.close();
        }
    }

    @Test
    void hasMatchesResolverForInheritanceAndDeny() throws Exception {
        var group = backend.getGroupData("staff");
        group.setPermissions(List.of("staff.chat", "staff.kick"), null);
        group.save();

        var user = backend.getUserData("alex");
        user.setParents(List.of("staff"), null);
        user.setPermissions(List.of("-staff.kick"), null);
        user.save();

        EffectiveUser effective = backend.resolveEffectiveUser("alex", PermissionContext.global());
        var permissionUser = manager.getUser("alex");

        assertEquals(effective.hasPermission("staff.chat"), permissionUser.has("staff.chat", null));
        assertEquals(effective.hasPermission("staff.kick"), permissionUser.has("staff.kick", null));
        assertTrue(permissionUser.has("staff.chat", null));
        assertFalse(permissionUser.has("staff.kick", null));
    }

    @Test
    void hasMatchesResolverForWildcards() throws Exception {
        var group = backend.getGroupData("mod");
        group.setPermissions(List.of("essentials.*"), null);
        group.save();

        var user = backend.getUserData("moduser");
        user.setParents(List.of("mod"), null);
        user.setPermissions(List.of("-essentials.ban"), null);
        user.save();

        EffectiveUser effective = backend.resolveEffectiveUser("moduser", PermissionContext.global());
        var permissionUser = manager.getUser("moduser");
        var matcher = manager.getPermissionMatcher();

        assertEquals(
                effective.hasPermission(matcher, "essentials.home", null),
                permissionUser.has("essentials.home", null));
        assertEquals(
                effective.hasPermission(matcher, "essentials.ban", null),
                permissionUser.has("essentials.ban", null));
        assertTrue(permissionUser.has("essentials.home", null));
        assertFalse(permissionUser.has("essentials.ban", null));
    }
}
