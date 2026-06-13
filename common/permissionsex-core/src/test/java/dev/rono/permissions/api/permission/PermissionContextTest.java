package dev.rono.permissions.api.permission;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link PermissionContext} map builders and world resolution. */
class PermissionContextTest {

    @Test
    void ofBuildsImmutableContextWithPresentKeys() {
        var ctx = PermissionContext.of("w", "s", "r", "creative");
        assertEquals("w", ctx.get(PermissionContext.WORLD));
        assertEquals("s", ctx.get(PermissionContext.SERVER));
        assertEquals("r", ctx.get(PermissionContext.REGION));
        assertEquals("creative", ctx.get(PermissionContext.GAMEMODE));
        assertThrows(UnsupportedOperationException.class, () -> ctx.put("x", "y"));
    }

    @Test
    void withStateIncludesOptionalStateKey() {
        var ctx = PermissionContext.withState("event-world", "minigame");
        assertEquals("event-world", ctx.get(PermissionContext.WORLD));
        assertEquals("minigame", ctx.get(PermissionContext.STATE));
    }

    @Test
    void resolveWorldPrefersWorldThenServer() {
        assertNull(PermissionContext.resolveWorld(null));
        assertNull(PermissionContext.resolveWorld(PermissionContext.global()));
        assertEquals("primary", PermissionContext.resolveWorld(PermissionContext.of("primary", null, null, null)));
        assertEquals("proxy", PermissionContext.resolveWorld(Map.of(PermissionContext.SERVER, "proxy")));
        assertEquals("world-wins", PermissionContext.resolveWorld(Map.of(
                PermissionContext.WORLD, "world-wins",
                PermissionContext.SERVER, "ignored")));
    }

    @Test
    void globalReturnsEmptyMap() {
        assertTrue(PermissionContext.global().isEmpty());
    }
}
