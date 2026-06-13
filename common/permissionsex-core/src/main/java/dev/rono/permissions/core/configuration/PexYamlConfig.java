package dev.rono.permissions.core.configuration;

import dev.rono.permissions.core.config.PexConfigData;
import dev.rono.permissions.core.config.PexConfigFlavor;

import java.util.LinkedHashMap;
import java.util.Map;

/** Locates {@code permissions:} in a loaded {@code config.yml} tree; delegates binding to {@link PexConfigData}. */
public final class PexYamlConfig {

    private PexYamlConfig() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> permissionsFrom(Map<String, Object> yamlRoot) {
        if (yamlRoot == null) {
            return new LinkedHashMap<>();
        }
        Object p = yamlRoot.get("permissions");
        if (!(p instanceof Map<?, ?> nested)) {
            return new LinkedHashMap<>();
        }
        return (Map<String, Object>) nested;
    }

    /** @see PexConfigData#fromPermissionsMap */
    public static PexConfigData load(
            Map<String, Object> permissions,
            java.util.function.Supplier<String> fallbackBasedir,
            PexConfigFlavor flavor) {
        return PexConfigData.fromPermissionsMap(permissions, fallbackBasedir, flavor);
    }
}
