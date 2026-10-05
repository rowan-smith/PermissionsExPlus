package ru.tehkode.permissions.backends.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import dev.rono.permissions.api.context.ContextSet;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.tehkode.permissions.PermissionManager;

class PermissionBackendTest {
    @TempDir
    Path directory;

    private PlusTestFixture fixture;
    private PermissionBackend backend;

    @BeforeEach
    void setUp() throws Exception {
        fixture = new PlusTestFixture(directory);
        backend = new PermissionBackend(mock(PermissionManager.class), null);
    }

    @AfterEach
    void tearDown() {
        if (fixture != null) {
            fixture.close();
        }
    }

    @Test
    void legacyGroupDataMutatesNodesParentsWeightAndReadsImplicitDefault() {
        var defaultData = backend.getGroupData("default");

        defaultData.save();

        backend.getGroupData("parent").setPermissions(List.of("example.parent"), null);

        var data = backend.getGroupData("staff");

        assertTrue(data.isVirtual());

        data.setPermissions(List.of("example.use", "-example.blocked"), "world_nether");

        data.setParents(List.of("parent"), null);

        data.setOption("prefix", "[Staff]", "world_nether");

        data.setOption("weight", "50", null);

        assertThrows(UnsupportedOperationException.class,
                () -> data.setOption("default", "true", "world_nether"));

        var staff = fixture.api().groups().cache().get("staff").orElseThrow();

        assertEquals(java.util.Set.of("example.use", "-example.blocked"),
                java.util.Set.copyOf(data.getPermissions("world_nether")));

        assertTrue(staff.hasDirectParent("parent", ContextSet.empty()));

        assertEquals(50, staff.weight().orElseThrow());

        assertEquals("[Staff]", data.getOption("prefix", "world_nether"));

        assertEquals("true", defaultData.getOption("default", "world_nether"));

        assertEquals("false", data.getOption("default", "world_nether"));
    }

    @Test
    void legacyUserDataCreatesAndUpdatesTheUser() {
        backend.getGroupData("member").save();

        var id = UUID.randomUUID();

        var data = backend.getUserData(id.toString());

        assertTrue(data.isVirtual());

        data.setOption("name", "Rono", null);

        data.setPermissions(List.of("example.use"), null);

        data.setParents(List.of("member"), "survival");

        var user = fixture.api().users().cache().get(id).orElseThrow();

        assertEquals("Rono", user.name());

        assertTrue(user.explicitlyAllows("example.use", ContextSet.empty()));

        assertTrue(user.hasDirectGroup("member", ContextSet.builder().add("world", "survival").build()));

        assertTrue(backend.hasUser(id.toString()));

        assertTrue(backend.getUserNames().contains("Rono"));
    }
}
