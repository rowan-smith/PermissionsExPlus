package ru.tehkode.permissions.spigot.bukkit;

import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts Bukkit {@link ConfigurationSection} subtrees into nested {@link Map}s for core configuration binding.
 */
final class BukkitYamlMaps {
    private BukkitYamlMaps() {}

    /** Deep-copy {@code permissions.*} into nested maps for {@link ru.tehkode.permissions.configuration.PexYamlConfig}. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> permissionsSection(ConfigurationSection permissions) {
        LinkedHashMap<String, Object> out = new LinkedHashMap<>();
        if (permissions == null) {
            return out;
        }
        for (String key : permissions.getKeys(false)) {
            Object raw = permissions.get(key);
            out.put(key, coerce(raw));
        }
        return out;
    }

    private static Object coerce(Object raw) {
        if (raw instanceof ConfigurationSection section) {
            LinkedHashMap<String, Object> nested = new LinkedHashMap<>();
            Set<String> keys = section.getKeys(false);
            if (!keys.isEmpty()) {
                for (String k : keys) {
                    nested.put(k, coerce(section.get(k)));
                }
                return nested;
            }
        }
        if (raw instanceof List<?> list) {
            return mapList(list);
        }
        return raw;
    }

    private static List<Object> mapList(List<?> list) {
        java.util.ArrayList<Object> mapped = new java.util.ArrayList<>(list.size());
        for (Object x : list) {
            mapped.add(coerce(x));
        }
        return mapped;
    }
}
