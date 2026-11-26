package ru.tehkode.permissions.bukkit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PermissionsExConfigTest {

    @Test
    public void testConfigDefaults() {
        YamlConfiguration yaml = new YamlConfiguration();
        PermissionsExConfig config = new PermissionsExConfig(yaml, null);
        
        assertEquals("file", config.getDefaultBackend());
        assertFalse(config.isDebug());
        assertFalse(config.allowOps());
        assertFalse(config.createUserRecords());
        assertEquals("plugins/PermissionsEx", config.getBasedir());
    }

    @Test
    public void testConfigOverrides() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("permissions.backend", "sql");
        yaml.set("permissions.debug", true);
        yaml.set("permissions.basedir", "custom/dir");
        
        PermissionsExConfig config = new PermissionsExConfig(yaml, null);
        
        assertEquals("sql", config.getDefaultBackend());
        assertTrue(config.isDebug());
        assertEquals("custom/dir", config.getBasedir());
    }

    @Test
    public void bstatsIsEnabledByDefaultAndWrittenToTheConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        PermissionsExConfig config = new PermissionsExConfig(yaml, null);

        assertTrue(config.bstatsEnabled());
        assertTrue(yaml.getBoolean("permissions.bstats"));
    }

    @Test
    public void bstatsCanBeTurnedOff() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("permissions.bstats", false);

        assertFalse(new PermissionsExConfig(yaml, null).bstatsEnabled());
    }
}
