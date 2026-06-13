package ru.tehkode.permissions.events;

import dev.rono.permissions.api.bus.EntityDispatch;
import dev.rono.permissions.api.bus.EntityMutation;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PermissionEventTest extends PEXTestBase {

    private List<EntityDispatch> entityDispatches() {
        return firedDispatches.stream()
                .filter(EntityDispatch.class::isInstance)
                .map(EntityDispatch.class::cast)
                .collect(Collectors.toList());
    }

    @Test
    public void testUserEvent() {
        PermissionUser user = manager.getUser("testUser");
        user.setPermissions(Collections.singletonList("test.perm"), null);

        assertEquals(1, entityDispatches().size(), "One event should be fired");
        EntityDispatch dispatch = entityDispatches().get(0);
        assertEquals(user.getIdentifier(), dispatch.entityIdentifier());
        assertEquals("USER", dispatch.entityType());
        assertEquals(EntityMutation.PERMISSIONS_CHANGED, dispatch.mutation());
    }

    @Test
    public void testGroupEvent() {
        PermissionGroup group = manager.getGroup("testGroup");
        group.setOption("test-opt", "val", null);

        assertEquals(1, entityDispatches().size(), "One event should be fired");
        EntityDispatch dispatch = entityDispatches().get(0);
        assertEquals(group.getIdentifier(), dispatch.entityIdentifier());
        assertEquals("GROUP", dispatch.entityType());
        assertEquals(EntityMutation.OPTIONS_CHANGED, dispatch.mutation());
    }

    @Test
    public void testInheritanceEvent() {
        PermissionUser user = manager.getUser("testUser");
        PermissionGroup group = manager.getGroup("testGroup");

        user.addGroup(group);

        assertTrue(
                entityDispatches().stream()
                        .anyMatch(
                                e ->
                                        e.entityIdentifier().equals(user.getIdentifier())
                                                && e.mutation() == EntityMutation.INHERITANCE_CHANGED));
    }
}
