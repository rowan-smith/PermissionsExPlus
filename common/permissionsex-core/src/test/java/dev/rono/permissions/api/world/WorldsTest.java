package dev.rono.permissions.api.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link Worlds} normalization helpers. */
class WorldsTest {

    @Test
    void isGlobalTreatsNullAndEmptyAsGlobal() {
        assertTrue(Worlds.isGlobal(null));
        assertTrue(Worlds.isGlobal(""));
        assertFalse(Worlds.isGlobal("world"));
    }

    @Test
    void normalizeTrimsAndCollapsesGlobal() {
        assertNull(Worlds.normalize(null));
        assertNull(Worlds.normalize(""));
        assertNull(Worlds.normalize("   "));
        assertEquals("arena", Worlds.normalize("  arena  "));
    }

    @Test
    void mapKeyAndFromMapKeyRoundTrip() {
        assertEquals("", Worlds.mapKey(null));
        assertEquals("survival", Worlds.mapKey("survival"));
        assertNull(Worlds.fromMapKey(""));
        assertNull(Worlds.fromMapKey(null));
        assertEquals("survival", Worlds.fromMapKey("survival"));
    }
}
