package ru.tehkode.permissions.spigot.bukkit;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class BukkitJarH2LayoutTest {

    private static Path jar;

    @BeforeAll
    static void locateJar() {
        jar = Path.of("target/permissionsex-spigot-3.0.0-SNAPSHOT.jar");
        Assumptions.assumeTrue(
                Files.isRegularFile(jar),
                () -> "Bukkit jar not built yet — run mvn package -pl bukkit -am");
    }

    @Test
    void shipsStandardH2DriverForExternalTools() throws IOException {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            assertNotNull(jarFile.getEntry("org/h2/Driver.class"),
                    "H2 must remain at org.h2 so permissions.mv.db works with standard JDBC tools");
            assertNull(jarFile.getEntry("ru/tehkode/libs/org/h2/Driver.class"),
                    "H2 must not be relocated inside the plugin jar");
        }
    }
}
