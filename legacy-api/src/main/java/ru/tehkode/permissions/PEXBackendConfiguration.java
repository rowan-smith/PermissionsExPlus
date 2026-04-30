package ru.tehkode.permissions;

import java.util.List;
import java.util.Map;

/**
 * Mutable configuration node used when constructing permission backends (classic Bukkit configuration shape).
 */
public interface PEXBackendConfiguration {

    String getName();

    String getString(String path);

    String getString(String path, String def);

    void set(String path, Object value);

    List<String> getStringList(String path);

    PEXBackendConfiguration getConfigurationSection(String path);

    PEXBackendConfiguration createSection(String path);

    boolean isConfigurationSection(String path);

    Map<String, Object> getValues(boolean deep);
}
