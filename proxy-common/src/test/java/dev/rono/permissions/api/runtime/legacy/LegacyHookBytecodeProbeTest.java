package dev.rono.permissions.api.runtime.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyHookBytecodeProbeTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsTehkodeBytecodeInJar() throws IOException {
        Path jar = tempDir.resolve("hook.jar");
        writeJar(jar, "com/example/Hook.class", "ru/tehkode/permissions/bukkit/PermissionsEx".getBytes());
        assertTrue(LegacyHookBytecodeProbe.referencesLegacyApiInJar(jar));
    }

    @Test
    void ignoresJarsWithoutTehkodeReferences() throws IOException {
        Path jar = tempDir.resolve("plain.jar");
        writeJar(jar, "com/example/Plain.class", new byte[] {1, 2, 3, 4});
        assertFalse(LegacyHookBytecodeProbe.referencesLegacyApiInJar(jar));
    }

    private static void writeJar(Path jar, String entryName, byte[] payload) throws IOException {
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry(entryName));
            out.write(payload);
            out.closeEntry();
        }
    }
}
