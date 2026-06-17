package dev.rono.permissions.bungee;

import dev.rono.permissions.runtime.legacy.ProxyLegacyHookPluginDetector;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

final class BungeeLegacyHookPluginDetector {

    private BungeeLegacyHookPluginDetector() {}

    static ProxyLegacyHookPluginDetector.Candidate findHook(PluginManager pluginManager, Plugin self) {
        List<ProxyLegacyHookPluginDetector.Candidate> candidates = new ArrayList<>();
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (plugin == self) {
                continue;
            }
            var description = plugin.getDescription();
            candidates.add(new ProxyLegacyHookPluginDetector.Candidate(
                    description.getName(),
                    description.getDepends(),
                    description.getSoftDepends(),
                    plugin.getClass().getProtectionDomain().getCodeSource().getLocation()));
        }
        return ProxyLegacyHookPluginDetector.findHook(candidates);
    }
}
