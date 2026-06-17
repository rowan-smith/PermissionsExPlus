package dev.rono.permissions.api.runtime.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Scans plugin archives for references to the classic {@code ru.tehkode.*} hook API.
 */
public final class LegacyHookBytecodeProbe {

    private static final String TEHKODE_MARKER = "ru/tehkode";

    private LegacyHookBytecodeProbe() {}

    public static boolean referencesLegacyApiAt(URL location) {
        if (location == null) {
            return false;
        }
        try {
            return referencesLegacyApiInJar(Path.of(location.toURI()));
        } catch (URISyntaxException | IllegalArgumentException ignored) {
            return false;
        }
    }

    public static boolean referencesLegacyApiInJar(Path jar) {
        if (!Files.isRegularFile(jar)) {
            return false;
        }
        try (JarInputStream jarStream = new JarInputStream(Files.newInputStream(jar))) {
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                if (containsTehkodeMarker(jarStream)) {
                    return true;
                }
            }
        } catch (IOException ignored) {
            return false;
        }
        return false;
    }

    private static boolean containsTehkodeMarker(InputStream classBytes) throws IOException {
        byte[] buffer = classBytes.readAllBytes();
        return new String(buffer, StandardCharsets.ISO_8859_1).contains(TEHKODE_MARKER);
    }
}
