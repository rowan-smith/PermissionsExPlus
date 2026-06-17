package dev.rono.permissions.api.realm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link Realms} normalization helpers. */
class RealmsTest {

    @Test
    void isGlobalTreatsNullAndEmptyAsGlobal() {
        assertTrue(Realms.isGlobal(null));
        assertTrue(Realms.isGlobal(""));
        assertFalse(Realms.isGlobal("lobby"));
    }

    @Test
    void normalizeTrimsAndCollapsesGlobal() {
        assertNull(Realms.normalize(null));
        assertNull(Realms.normalize(""));
        assertNull(Realms.normalize("   "));
        assertEquals("arena", Realms.normalize("  arena  "));
    }

    @Test
    void mapKeyAndFromMapKeyRoundTrip() {
        assertEquals("", Realms.mapKey(null));
        assertEquals("survival", Realms.mapKey("survival"));
        assertNull(Realms.fromMapKey(""));
        assertNull(Realms.fromMapKey(null));
        assertEquals("survival", Realms.fromMapKey("survival"));
    }
}
