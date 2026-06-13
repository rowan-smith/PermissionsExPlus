package dev.rono.permissions.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandFrameworkTest {

    @Test
    void defaultsToModern() {
        assertEquals(CommandFramework.MODERN, CommandFramework.fromConfig(null));
        assertEquals(CommandFramework.MODERN, CommandFramework.fromConfig(""));
    }

    @Test
    void parsesAliases() {
        assertEquals(CommandFramework.CLASSIC, CommandFramework.fromConfig("classic"));
        assertEquals(CommandFramework.CLASSIC, CommandFramework.fromConfig("legacy"));
        assertEquals(CommandFramework.MODERN, CommandFramework.fromConfig("modern"));
    }
}
