package dev.rono.permissions.velocity;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.meta.PluginDependency;
import dev.rono.permissions.runtime.legacy.ProxyLegacyHookPluginDetector;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class VelocityLegacyHookPluginDetector {

    private VelocityLegacyHookPluginDetector() {}

    static ProxyLegacyHookPluginDetector.Candidate findHook(PluginManager pluginManager, PluginContainer self) {
        List<ProxyLegacyHookPluginDetector.Candidate> candidates = new ArrayList<>();
        for (PluginContainer plugin : pluginManager.getPlugins()) {
            if (plugin.equals(self)) {
                continue;
            }
            var meta = plugin.getDescription();
            if (meta == null) {
                continue;
            }
            candidates.add(new ProxyLegacyHookPluginDetector.Candidate(
                    meta.getId(),
                    dependencyIds(meta.getDependencies()),
                    Collections.emptyList(),
                    codeSource(plugin)));
        }
        return ProxyLegacyHookPluginDetector.findHook(candidates);
    }

    private static Collection<String> dependencyIds(Collection<PluginDependency> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return List.of();
        }
        List<String> ids = new ArrayList<>(dependencies.size());
        for (PluginDependency dependency : dependencies) {
            ids.add(dependency.getId());
        }
        return ids;
    }

    private static URL codeSource(PluginContainer plugin) {
        return plugin.getInstance()
                .map(instance -> instance.getClass().getProtectionDomain().getCodeSource().getLocation())
                .orElse(null);
    }
}
