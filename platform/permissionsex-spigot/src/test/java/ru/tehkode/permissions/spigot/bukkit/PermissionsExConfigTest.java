package ru.tehkode.permissions.spigot.bukkit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PermissionsExConfigTest {

    @Test
    public void testConfigDefaults() {
        YamlConfiguration yaml = new YamlConfiguration();
        SpigotPermissionsExPlugin plugin = mockPlugin(yaml);

        PermissionsExConfig config = new PermissionsExConfig(plugin);

        assertEquals("file", config.getDefaultBackend());
        assertFalse(config.isDebug());
        assertFalse(config.allowOps());
        assertFalse(config.createUserRecords());
        assertEquals("plugins/PermissionsEx", config.getBasedir());
        assertEquals("permissions.yml",
                config.options().current().storeRelative());
        assertEquals("plugins/PermissionsEx/permissions.yml",
                config.options().current().storePathSlash());
    }

    @Test
    public void testConfigOverrides() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("permissions.backend", "sql");
        yaml.set("permissions.debug", true);
        yaml.set("permissions.basedir", "custom/dir");
        yaml.set("permissions.backends.file.file", "customperms.yml");

        SpigotPermissionsExPlugin plugin = mockPlugin(yaml);
        PermissionsExConfig config = new PermissionsExConfig(plugin);

        assertEquals("sql", config.getDefaultBackend());
        assertTrue(config.isDebug());
        assertEquals("custom/dir", config.getBasedir());
        assertEquals("customperms.yml",
                config.options().current().storeRelative());
        assertEquals("custom/dir/customperms.yml",
                config.options().current().storePathSlash());
    }

    private static SpigotPermissionsExPlugin mockPlugin(YamlConfiguration yaml) {
        SpigotPermissionsExPlugin plugin = Mockito.mock(SpigotPermissionsExPlugin.class);
        when(plugin.getConfig()).thenReturn(yaml);

        when(plugin.getDescription()).thenReturn(realDescriptionFile());

        when(plugin.getDataFolder()).thenReturn(new File(System.getProperty("java.io.tmpdir")));

        doNothing().when(plugin).saveConfig();
        return plugin;
    }

    /** {@link PluginDescriptionFile} is final; Mockito cannot mock it without inline mocking. */
    private static PluginDescriptionFile realDescriptionFile() {
        String yaml = ""
                + "name: PermissionsEx\n"
                + "version: 1.0.0-test\n"
                + "main: ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin\n"
                + "api-version: 1.21\n";
        try {
            return new PluginDescriptionFile(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        } catch (InvalidDescriptionException e) {
            throw new AssertionError(e);
        }
    }
}
