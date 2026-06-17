package dev.rono.permissions.api.permission;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PermissionContextApiCoreTest {

    @Test
    void blankValuesCollapseToGlobal() {
        assertSame(PermissionContext.global(), PermissionContext.of(null));
        assertSame(PermissionContext.global(), PermissionContext.of(Map.of()));
        assertTrue(PermissionContext.server(null).isGlobal());
        assertTrue(PermissionContext.server("").isGlobal());
        assertTrue(PermissionContext.world((String) null).isGlobal());
        assertTrue(PermissionContext.world("").isGlobal());
    }

    @Test
    void getFiltersEmptyAttributeValues() {
        var ctx = PermissionContext.of(Map.of(PermissionContext.WORLD, ""));
        assertTrue(ctx.get(PermissionContext.WORLD).isEmpty());
    }

    @Test
    void fromMapRoundTrips() {
        var original = PermissionContext.of("world", "server", "region", "creative");
        var roundTrip = PermissionContext.fromMap(original.toMap());
        assertEquals(original.toMap(), roundTrip.toMap());
    }
}
