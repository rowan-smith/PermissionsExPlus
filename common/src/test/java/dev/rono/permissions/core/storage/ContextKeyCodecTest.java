package dev.rono.permissions.core.storage;

import dev.rono.permissions.api.permission.PermissionContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextKeyCodecTest {

    @Test
    void encodesGlobalAsNull() {
        assertNull(ContextKeyCodec.encode(PermissionContext.global()));
    }

    @Test
    void encodesWorldContext() {
        assertEquals("world:nether", ContextKeyCodec.encode(PermissionContext.world("nether")));
    }

    @Test
    void matchesSpecificContextAgainstBroaderRequest() {
        assertTrue(ContextKeyCodec.matches("world:nether|region:spawn", "world:nether"));
        assertFalse(ContextKeyCodec.matches("world:overworld", "world:nether"));
    }

    @Test
    void specificityOrdersContexts() {
        assertTrue(ContextKeyCodec.specificity("world:a|region:b")
                > ContextKeyCodec.specificity("world:a"));
    }
}
