package ru.tehkode.permissions.bukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

public class PermissionsExConfigTest {

    @Test
    public void facadeIgnoresBukkitYamlAndDefaultsToDataBackend() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("permissions.backend", "sql");
        yaml.set("permissions.debug", true);
        yaml.set("permissions.basedir", "custom/dir");
        yaml.set("permissions.allowOps", true);

        PermissionsExConfig config = new PermissionsExConfig(yaml, null);

        assertEquals("data", config.getDefaultBackend());
        assertFalse(config.isDebug());
        assertFalse(config.allowOps());
        assertFalse(config.createUserRecords());
        assertFalse(config.shouldLogPlayers());
        assertFalse(config.saveDefaultGroup());
        assertFalse(config.informPlayers());
        assertEquals("plugins/PermissionsEx", config.getBasedir());
        assertEquals("data", config.getBackendConfig("data").getString("type"));
    }

    @Test
    public void saveIsNoOp() {
        PermissionsExConfig config = new PermissionsExConfig(null);
        config.save();
    }
}
