package ru.tehkode.permissions.backends;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.backends.data.PlusTestFixture;
import ru.tehkode.permissions.backends.file.FileBackend;
import ru.tehkode.permissions.backends.memory.MemoryBackend;
import ru.tehkode.permissions.backends.sql.SQLBackend;

class BackendAliasRedirectTest {
    @TempDir
    Path directory;

    private PlusTestFixture fixture;
    private PermissionManager manager;

    @BeforeEach
    void setUp() throws Exception {
        fixture = new PlusTestFixture(directory);

        manager = mock(PermissionManager.class);
        when(manager.getLogger()).thenReturn(Logger.getLogger("PEX-test"));

        PermissionBackend.registerBackendAlias("data", ru.tehkode.permissions.backends.data.PermissionBackend.class);
        PermissionBackend.registerBackendAlias("file", FileBackend.class);
        PermissionBackend.registerBackendAlias("sql", SQLBackend.class);
        PermissionBackend.registerBackendAlias("memory", MemoryBackend.class);
        PermissionBackend.registerBackendAlias("multi", MultiBackend.class);
    }

    @AfterEach
    void tearDown() {
        if (fixture != null) {
            fixture.close();
        }
    }

    @Test
    void defaultBackendIsData() {
        assertEquals("data", PermissionBackend.DEFAULT_BACKEND);
    }

    @Test
    void fileSqlMemoryAndMultiAliasesRedirectToDataBridge() throws Exception {
        var config = new YamlConfiguration();

        var file = PermissionBackend.getBackend("file", manager, config, null);
        var sql = PermissionBackend.getBackend("sql", manager, config, null);
        var memory = PermissionBackend.getBackend("memory", manager, config, null);
        var multi = PermissionBackend.getBackend("multi", manager, config, null);
        var data = PermissionBackend.getBackend("data", manager, config, null);

        assertInstanceOf(FileBackend.class, file);
        assertInstanceOf(SQLBackend.class, sql);
        assertInstanceOf(MemoryBackend.class, memory);
        assertInstanceOf(MultiBackend.class, multi);
        assertInstanceOf(ru.tehkode.permissions.backends.data.PermissionBackend.class, data);

        var id = UUID.randomUUID();
        file.getUserData(id.toString()).setPermissions(List.of("alias.redirect"), null);
        file.getUserData(id.toString()).setOption("prefix", "[R]", null);

        assertTrue(sql.hasUser(id.toString()));
        assertTrue(memory.hasUser(id.toString()));
        assertEquals(List.of("alias.redirect"), multi.getUserData(id.toString()).getPermissions(null));
        assertEquals("[R]", data.getUserData(id.toString()).getOption("prefix", null));
    }

    @Test
    void sqlGetSqlIsUnavailable() throws Exception {
        var backend = new SQLBackend(manager, new YamlConfiguration());
        assertThrows(Exception.class, backend::getSQL);
    }
}
