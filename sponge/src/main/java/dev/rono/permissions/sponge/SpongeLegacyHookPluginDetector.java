package dev.rono.permissions.sponge;

import dev.rono.permissions.runtime.legacy.ProxyLegacyHookPluginDetector;
import org.spongepowered.plugin.PluginContainer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class SpongeLegacyHookPluginDetector {

    private SpongeLegacyHookPluginDetector() {}

    static ProxyLegacyHookPluginDetector.Candidate findHook(Iterable<PluginContainer> plugins, PluginContainer self) {
        List<ProxyLegacyHookPluginDetector.Candidate> candidates = new ArrayList<>();
        for (PluginContainer plugin : plugins) {
            if (plugin.equals(self)) {
                continue;
            }
            var meta = plugin.metadata();
            candidates.add(new ProxyLegacyHookPluginDetector.Candidate(
                    meta.id(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    codeSource(plugin)));
        }
        return ProxyLegacyHookPluginDetector.findHook(candidates);
    }

    private static URL codeSource(PluginContainer plugin) {
        Object instance = plugin.instance();
        if (instance == null) {
            return null;
        }
        return instance.getClass().getProtectionDomain().getCodeSource().getLocation();
    }
}
