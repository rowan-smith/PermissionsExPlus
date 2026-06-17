package dev.rono.permissions.spigot.legacy;

import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyHookPluginDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsPexDepend() throws InvalidDescriptionException {
        var description = description("""
                name: HookPlugin
                version: 1.0
                main: com.example.Hook
                depend: [PermissionsEx]
                """);
        assertTrue(LegacyHookPluginDetector.declaresPexRelationship(description));
    }

    @Test
    void detectsPexSoftDepend() throws InvalidDescriptionException {
        var description = description("""
                name: HookPlugin
                version: 1.0
                main: com.example.Hook
                softdepend: [PermissionsEx]
                """);
        assertTrue(LegacyHookPluginDetector.declaresPexRelationship(description));
    }

    @Test
    void ignoresUnrelatedPlugins() throws InvalidDescriptionException {
        var description = description("""
                name: Other
                version: 1.0
                main: com.example.Other
                depend: [Vault]
                """);
        assertFalse(LegacyHookPluginDetector.declaresPexRelationship(description));
    }

    @Test
    void detectsTehkodeBytecodeInJar() throws IOException {
        Path jar = tempDir.resolve("hook.jar");
        writeJar(jar, "com/example/Hook.class", "ru/tehkode/permissions/bukkit/PermissionsEx".getBytes());
        assertTrue(LegacyHookPluginDetector.referencesLegacyApiInJar(jar));
    }

    @Test
    void ignoresJarsWithoutTehkodeReferences() throws IOException {
        Path jar = tempDir.resolve("plain.jar");
        writeJar(jar, "com/example/Plain.class", new byte[] {1, 2, 3, 4});
        assertFalse(LegacyHookPluginDetector.referencesLegacyApiInJar(jar));
    }

    private static PluginDescriptionFile description(String yaml) throws InvalidDescriptionException {
        return new PluginDescriptionFile(new StringReader(yaml));
    }

    private static void writeJar(Path jar, String entryName, byte[] payload) throws IOException {
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry(entryName));
            out.write(payload);
            out.closeEntry();
        }
    }
}
