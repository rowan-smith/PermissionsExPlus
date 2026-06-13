package dev.rono.permissions.api.permission;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link PermissionContext}. */
class PermissionContextTest {

    @Test
    void ofBuildsImmutableContextWithPresentKeys() {
        var ctx = PermissionContext.of("w", "s", "r", "creative");
        assertEquals("w", ctx.get(PermissionContext.WORLD).orElseThrow());
        assertEquals("s", ctx.get(PermissionContext.SERVER).orElseThrow());
        assertEquals("r", ctx.get(PermissionContext.REGION).orElseThrow());
        assertEquals("creative", ctx.get(PermissionContext.GAMEMODE).orElseThrow());
        assertThrows(UnsupportedOperationException.class, () -> ctx.attributes().put("x", "y"));
    }

    @Test
    void withStateIncludesOptionalStateKey() {
        var ctx = PermissionContext.withState("event-world", "minigame");
        assertEquals("event-world", ctx.get(PermissionContext.WORLD).orElseThrow());
        assertEquals("minigame", ctx.get(PermissionContext.STATE).orElseThrow());
    }

    @Test
    void worldAndServerFactories() {
        assertTrue(PermissionContext.global().isGlobal());
        assertEquals("lobby", PermissionContext.server("lobby").get(PermissionContext.SERVER).orElseThrow());
        assertEquals("survival", PermissionContext.world("survival").get(PermissionContext.WORLD).orElseThrow());
        assertEquals("world-wins", PermissionContext.of(Map.of(
                PermissionContext.WORLD, "world-wins",
                PermissionContext.SERVER, "ignored")).get(PermissionContext.WORLD).orElseThrow());
    }

    @Test
    void globalReturnsEmptyAttributes() {
        assertTrue(PermissionContext.global().attributes().isEmpty());
    }
}
