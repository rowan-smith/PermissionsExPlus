package dev.rono.permissions.core.config;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PexConfigDataCommandFrameworkTest {

    @Test
    void loadsCommandFrameworkFromYamlMap() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put(PexConfigData.KEY_COMMAND_FRAMEWORK, "classic");
        PexConfigData data = PexConfigData.fromPermissionsMap(root, () -> ".", PexConfigFlavor.SPIGOT);
        assertEquals(CommandFramework.CLASSIC, data.commandFramework());
    }

    @Test
    void defaultsCommandFrameworkToModern() {
        PexConfigData data = PexConfigData.fromPermissionsMap(Map.of(), () -> ".", PexConfigFlavor.SPIGOT);
        assertEquals(CommandFramework.MODERN, data.commandFramework());
    }
}
