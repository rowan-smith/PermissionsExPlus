package dev.rono.permissions.api.bus;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PermissionDispatchTypesTest {

    @Test
    void entityDispatchImplementsPermissionDispatch() {
        var dispatch = new EntityDispatch(
                UUID.randomUUID(), "alice", "USER", EntityMutation.PERMISSIONS_CHANGED);
        assertInstanceOf(PermissionDispatch.class, dispatch);
        assertEquals("alice", dispatch.entityIdentifier());
        assertEquals(EntityMutation.PERMISSIONS_CHANGED, dispatch.mutation());
    }

    @Test
    void systemDispatchImplementsPermissionDispatch() {
        var dispatch = new SystemDispatch(UUID.randomUUID(), SystemMutation.RELOADED);
        assertInstanceOf(PermissionDispatch.class, dispatch);
        assertEquals(SystemMutation.RELOADED, dispatch.mutation());
    }

    @Test
    void permissionDispatchIsSealedToKnownImplementations() {
        var permitted = Set.of(EntityDispatch.class.getName(), SystemDispatch.class.getName());
        var actual = java.util.Arrays.stream(PermissionDispatch.class.getPermittedSubclasses())
                .map(Class::getName)
                .collect(Collectors.toSet());
        assertEquals(permitted, actual);
    }
}
