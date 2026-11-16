package ru.tehkode.permissions.backends.data;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import dev.rono.permissions.api.context.ContextSet;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.tehkode.permissions.PermissionManager;
import dev.rono.permissions.core.util.Immutables;

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

        backend.getGroupData("parent").setPermissions(Immutables.listOf("example.parent"), null);

        var data = backend.getGroupData("staff");

        assertTrue(data.isVirtual());

        data.setPermissions(Immutables.listOf("example.use", "-example.blocked"), "world_nether");

        data.setParents(Immutables.listOf("parent"), null);

        data.setOption("prefix", "[Staff]", "world_nether");

        data.setOption("weight", "50", null);

        assertThrows(UnsupportedOperationException.class,
                () -> data.setOption("default", "true", "world_nether"));

        var staff = fixture.api().groups().cache().get("staff").get();

        assertEquals(Immutables.setOf("example.use", "-example.blocked"),
                Immutables.copyOfSet(data.getPermissions("world_nether")));

        assertTrue(staff.hasDirectParent("parent", ContextSet.empty()));

        assertEquals(50, staff.weight().getAsInt());

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

        data.setPermissions(Immutables.listOf("example.use"), null);

        data.setParents(Immutables.listOf("member"), "survival");

        var user = fixture.api().users().cache().get(id).get();

        assertEquals("Rono", user.name());

        assertTrue(user.explicitlyAllows("example.use", ContextSet.empty()));

        assertTrue(user.hasDirectGroup("member", ContextSet.builder().add("world", "survival").build()));

        assertTrue(backend.hasUser(id.toString()));

        assertTrue(backend.getUserNames().contains("Rono"));
    }
}
