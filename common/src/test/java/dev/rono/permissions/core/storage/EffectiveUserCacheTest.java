package dev.rono.permissions.core.storage;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.core.storage.model.Group;
import dev.rono.permissions.core.storage.model.Ladder;
import dev.rono.permissions.core.storage.model.User;
import dev.rono.permissions.core.storage.resolution.EffectiveUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EffectiveUserCacheTest {

    private LocalSqlRepository repository;
    private EffectiveUserCache cache;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        repository = LocalSqlRepository.inMemory("cache-test-" + UUID.randomUUID());
        repository.deploySchema();
        repository.ensureSchemaLatest();
        cache = new EffectiveUserCache();
        userId = UUID.randomUUID();
        repository.upsertUser(userId, "player", Instant.now(), Instant.now());
        repository.replaceUserPermissions(userId, null, List.of("cached.perm"));
    }

    @AfterEach
    void tearDown() {
        repository.close();
    }

    @Test
    void cachesResolvedUsersUntilInvalidated() throws SQLException {
        EffectiveUser first = cache.getOrResolve(
                userId,
                PermissionContext.global(),
                this::loadUser,
                this::loadGroups,
                this::loadLadders);
        assertEquals(1, cache.size());

        EffectiveUser second = cache.getOrResolve(
                userId,
                PermissionContext.global(),
                () -> {
                    throw new IllegalStateException("Should be cached");
                },
                this::loadGroups,
                this::loadLadders);
        assertSame(first, second);

        cache.invalidateUser(userId);
        assertEquals(0, cache.size());
        assertTrue(cache.generation() > 0);
    }

    @Test
    void groupInvalidationClearsAllEntries() throws Exception {
        cache.getOrResolve(userId, PermissionContext.global(), this::loadUser,
                this::loadGroups, this::loadLadders);
        cache.invalidateGroup(1);
        assertEquals(0, cache.size());
    }

    private User loadUser() {
        try {
            return repository.loadUser(userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<Integer, Group> loadGroups() {
        try {
            return repository.loadAllGroups();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Ladder> loadLadders() {
        try {
            return repository.loadLadders();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
