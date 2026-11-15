package dev.rono.permissions.core.context;

import lombok.var;

import dev.rono.permissions.core.config.AdvancedConfiguration;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import dev.rono.permissions.core.util.Immutables;

/**
 * Core-owned decision of which platform-provided context values affect
 * resolution.
 */
public final class ContextPolicy {
    private final Set<String> enabledKeys;

    public ContextPolicy(AdvancedConfiguration configuration) {
        var keys = new LinkedHashSet<String>();

        if (configuration.trackWorlds()) {
            keys.add("world");
        }

        if (configuration.trackGamemodes()) {
            keys.add("gamemode");
        }

        if (configuration.trackServers()) {
            keys.add("server");
        }

        if (configuration.trackProxies()) {
            keys.add("proxy");
        }

        enabledKeys = Immutables.copyOfSet(keys);
    }

    public Map<String, String> select(Map<String, String> values) {
        var selected = new LinkedHashMap<String, String>();

        values.forEach((key, value) -> {
            if (enabledKeys.contains(key) && value != null) {
                selected.put(key, value);
            }
        });

        return Immutables.copyOfMap(selected);
    }
}
