package dev.rono.permissions.spigot.legacy;

import dev.rono.permissions.api.runtime.legacy.LegacyHookBytecodeProbe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import java.util.Collection;
import java.util.Locale;

/**
 * Detects third-party plugins that compile against or depend on the classic {@code ru.tehkode.*} hook API.
 */
public final class LegacyHookPluginDetector {

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
        return LegacyHookBytecodeProbe.referencesLegacyApiAt(
                plugin.getClass().getProtectionDomain().getCodeSource().getLocation());
    }

    static boolean referencesLegacyApiInJar(java.nio.file.Path jar) {
        return LegacyHookBytecodeProbe.referencesLegacyApiInJar(jar);
    }
}
