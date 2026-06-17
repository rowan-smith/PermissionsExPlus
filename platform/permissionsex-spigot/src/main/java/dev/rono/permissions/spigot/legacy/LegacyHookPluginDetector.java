package dev.rono.permissions.spigot.legacy;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Detects third-party plugins that compile against or depend on the classic {@code ru.tehkode.*} hook API.
 */
public final class LegacyHookPluginDetector {

    private static final String TEHKODE_MARKER = "ru/tehkode";
    private static final String PEX_NAME = "permissionsex";

    private LegacyHookPluginDetector() {}

    /**
     * @return the first non-PEX plugin that appears to hook the legacy API, or {@code null}
     */
    public static Plugin findHookPlugin(PluginManager pluginManager, Plugin self) {
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (plugin == self) {
                continue;
            }
            if (declaresPexRelationship(plugin.getDescription()) || referencesLegacyApi(plugin)) {
                return plugin;
            }
        }
        return null;
    }

    public static boolean anyHookPlugin(PluginManager pluginManager, Plugin self) {
        return findHookPlugin(pluginManager, self) != null;
    }

    static boolean declaresPexRelationship(PluginDescriptionFile description) {
        return mentionsPex(description.getDepend())
                || mentionsPex(description.getSoftDepend())
                || mentionsPex(description.getLoadBefore());
    }

    private static boolean mentionsPex(Collection<String> names) {
        if (names == null || names.isEmpty()) {
            return false;
        }
        for (String name : names) {
            if (name != null && PEX_NAME.equals(name.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    static boolean referencesLegacyApi(Plugin plugin) {
        try {
            var location = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return false;
            }
            return referencesLegacyApiInJar(Path.of(location.toURI()));
        } catch (URISyntaxException | IllegalArgumentException ignored) {
            return false;
        }
    }

    static boolean referencesLegacyApiInJar(Path jar) {
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
