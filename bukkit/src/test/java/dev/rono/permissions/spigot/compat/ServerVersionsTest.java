package dev.rono.permissions.spigot.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerVersionsTest {

    @Test
    void comparesMinecraftVersions() {
        assertTrue(ServerVersions.compare("1.8.8", "1.26.1") < 0);
        assertTrue(ServerVersions.compare("1.21.11", "1.21.11") == 0);
        assertTrue(ServerVersions.compare("1.26.1", "1.8.8") > 0);
    }

    @Test
    void declaresSupportedRange() {
        assertEquals("1.8.8", ServerVersions.MIN_MC);
        assertEquals("1.26.1", ServerVersions.MAX_MC);
    }
}
