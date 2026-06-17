package dev.rono.permissions.core.storage.backend;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.core.storage.LocalSqlRepository;
import dev.rono.permissions.core.storage.resolution.EffectiveUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocalSqlBackendTest extends ru.tehkode.permissions.PEXTestBase {

    private LocalSqlRepository repository;
    private LocalSqlBackend backend;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        repository = LocalSqlRepository.inMemory("backend-" + System.nanoTime());
        repository.deploySchema();
        repository.setSchemaVersion(LocalSqlRepository.SCHEMA_VERSION);
        backend = new LocalSqlBackend(manager, repository, java.io.File.createTempFile("unused", ".yml"));
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
    void persistsAndResolvesPermissions() throws Exception {
        var group = backend.getGroupData("staff");
        group.setPermissions(List.of("staff.chat"), null);
        group.save();

        var user = backend.getUserData("alex");
        user.setParents(List.of("staff"), null);
        user.setPermissions(List.of("-staff.chat"), null);
        user.save();

        EffectiveUser effective = backend.resolveEffectiveUser("alex", PermissionContext.global());
        assertFalse(effective.hasPermission("staff.chat"));
        assertTrue(backend.hasUser("alex"));
        assertTrue(backend.hasGroup("staff"));
    }

    @Test
    void invalidatesCacheAfterUserMutation() throws Exception {
        var user = backend.getUserData("cached");
        user.setPermissions(List.of("a"), null);
        user.save();

        EffectiveUser first = backend.resolveEffectiveUser("cached", PermissionContext.global());
        assertTrue(first.hasPermission("a"));

        user.setPermissions(List.of(), null);
        user.save();

        EffectiveUser second = backend.resolveEffectiveUser("cached", PermissionContext.global());
        assertNotSame(first, second);
        assertFalse(second.hasPermission("a"));
    }

    @Test
    void diagnosticLabelIncludesDatabaseName() {
        assertTrue(backend.diagnosticLabel().startsWith("h2:"));
    }

    @Test
    void runtimeHasUsesLocalResolverWithWildcards() throws Exception {
        assertInstanceOf(dev.rono.permissions.core.DefaultPermissionManager.class, manager);
        ((dev.rono.permissions.core.DefaultPermissionManager) manager).adoptBackend(backend);

        var group = backend.getGroupData("staff");
        group.setPermissions(List.of("essentials.*"), null);
        group.save();

        var user = backend.getUserData("player");
        user.setParents(List.of("staff"), null);
        user.setPermissions(List.of("-essentials.ban"), null);
        user.save();

        var permissionUser = manager.getUser("player");
        assertFalse(permissionUser.has("essentials.ban", null));
        assertTrue(permissionUser.has("essentials.home", null));

        assertTrue(dev.rono.permissions.core.storage.resolution.LocalPermissionEvaluator.hasUser(
                manager, "player", "essentials.home", PermissionContext.global()));
        assertFalse(dev.rono.permissions.core.storage.resolution.LocalPermissionEvaluator
                .resolvedPermissions(manager, "player", PermissionContext.global())
                .isEmpty());
    }

    @Test
    void ladderRankSurvivesRoundTrip() throws Exception {
        var group = backend.getGroupData("vip");
        group.setOption("rank-ladder", "staff", null);
        group.setOption("rank", "5", null);
        group.save();

        assertEquals("5", group.getOption("rank", null));
        assertEquals("staff", group.getOption("rank-ladder", null));
    }
}
