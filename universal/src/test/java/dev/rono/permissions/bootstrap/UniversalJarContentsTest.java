package dev.rono.permissions.bootstrap;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniversalJarContentsTest {

    private static Path jar;

    @BeforeAll
    static void locateJar() {
        jar = Path.of("target/PermissionsExPlus-3.0.0-SNAPSHOT.jar");
        Assumptions.assumeTrue(
                Files.isRegularFile(jar),
                () -> "Universal jar not built yet — run mvn package -pl universal -am");
    }

    @Test
    void containsAllPlatformEntryPoints() throws IOException {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            assertNotNull(jarFile.getEntry("ru/tehkode/permissions/spigot/bukkit/SpigotPermissionsExPlugin.class"));
            assertNotNull(jarFile.getEntry("dev/rono/permissions/bungee/BungeePermissionsExPlugin.class"));
            assertNotNull(jarFile.getEntry("dev/rono/permissions/velocity/VelocityPermissionsExPlugin.class"));
            assertNotNull(jarFile.getEntry("dev/rono/permissions/sponge/SpongePermissionsExPlugin.class"));
        }
    }

    @Test
    void containsPlatformDescriptors() throws IOException {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            assertNotNull(jarFile.getEntry("plugin.yml"));
            assertNotNull(jarFile.getEntry("bungee.yml"));
            assertNotNull(jarFile.getEntry("velocity-plugin.json"));
            assertTrue(jarFile.stream().anyMatch(entry -> entry.getName().contains("sponge_plugins.json")));
        }
    }
}
