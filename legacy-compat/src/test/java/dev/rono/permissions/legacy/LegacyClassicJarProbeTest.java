package dev.rono.permissions.legacy;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Optional regression probe: drop classic third-party plugin JARs into
 * {@code src/test/resources/plugin-jars/} to verify they still resolve {@link PermissionsEx} static entry points.
 */
class LegacyClassicJarProbeTest {

    @Test
    void optionalClassicPluginJarsResolveLegacyEntryPoints() throws Exception {
        Path dir = Path.of("src/test/resources/plugin-jars");
        if (!Files.isDirectory(dir)) {
            Assumptions.assumeTrue(false, "No plugin-jars directory — add classic hook plugin JARs to enable this probe");
        }
        try (Stream<Path> jars = Files.list(dir).filter(p -> p.toString().endsWith(".jar"))) {
            Path[] files = jars.toArray(Path[]::new);
            Assumptions.assumeTrue(files.length > 0, "plugin-jars directory is empty");
            for (Path jar : files) {
                probeJar(jar);
            }
        }
    }

    private static void probeJar(Path jar) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {jar.toUri().toURL()}, null)) {
            Class<?> permissionsEx = Class.forName("ru.tehkode.permissions.bukkit.PermissionsEx", false, loader);
            permissionsEx.getMethod("getPermissionManager");
            permissionsEx.getMethod("isAvailable");
        }
    }
}
