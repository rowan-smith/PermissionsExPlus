package dev.rono.permissions.runtime.legacy;

import dev.rono.permissions.api.runtime.legacy.LegacyHookBytecodeProbe;

import java.net.URL;
import java.util.Collection;
import java.util.Locale;

/**
 * Detects proxy-side plugins that depend on or compile against the classic PEX hook API.
 */
public final class ProxyLegacyHookPluginDetector {

    private static final String PEX_NAME = "permissionsex";

    private ProxyLegacyHookPluginDetector() {}

    public record Candidate(
            String name,
            Collection<String> hardDepends,
            Collection<String> softDepends,
            URL codeSource) {}

    public static Candidate findHook(Iterable<Candidate> plugins) {
        for (Candidate plugin : plugins) {
            if (declaresPexRelationship(plugin) || LegacyHookBytecodeProbe.referencesLegacyApiAt(plugin.codeSource())) {
                return plugin;
            }
        }
        return null;
    }

    static boolean declaresPexRelationship(Candidate plugin) {
        return mentionsPex(plugin.hardDepends()) || mentionsPex(plugin.softDepends());
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
}
