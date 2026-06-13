package ru.tehkode.permissions;

import java.util.List;
import java.util.Map;

/**
 * Mutable configuration node used when constructing permission backends (classic Bukkit configuration shape).
 *
 * <p>Backends receive an instance of this interface at construction time. It mirrors a subset of
 * Bukkit's {@code ConfigurationSection} API so backend plugins can read connection settings, table
 * names, and other storage parameters without depending on a concrete config implementation.</p>
 */
public interface PEXBackendConfiguration {

    /**
     * Returns the name of this configuration section.
     *
     * @return section name; never {@code null}
     */
    String getName();

    /**
     * Returns the string value at the given path.
     *
     * @param path configuration path
     * @return string value, or {@code null} if the path is not set
     */
    String getString(String path);

    /**
     * Returns the string value at the given path, or a default when unset.
     *
     * @param path configuration path
     * @param def  value to return when the path is not set
     * @return string value at {@code path}, or {@code def}
     */
    String getString(String path, String def);

    /**
     * Sets a value at the given configuration path.
     *
     * @param path  configuration path
     * @param value value to store; may be a scalar, list, or nested section
     */
    void set(String path, Object value);

    /**
     * Returns the string list at the given path.
     *
     * @param path configuration path
     * @return list of strings; never {@code null} (empty list if unset)
     */
    List<String> getStringList(String path);

    /**
     * Returns the nested configuration section at the given path.
     *
     * @param path configuration path
     * @return nested section, or {@code null} if the path is not a section
     */
    PEXBackendConfiguration getConfigurationSection(String path);

    /**
     * Creates (or returns) a nested configuration section at the given path.
     *
     * @param path configuration path for the new section
     * @return the created or existing section; never {@code null}
     */
    PEXBackendConfiguration createSection(String path);

    /**
     * Returns whether the value at the given path is a configuration section.
     *
     * @param path configuration path
     * @return {@code true} if {@code path} points to a nested section
     */
    boolean isConfigurationSection(String path);

    /**
     * Returns all key-value pairs in this section.
     *
     * @param deep {@code true} to include values from nested sections recursively
     * @return map of path keys to values; never {@code null}
     */
    Map<String, Object> getValues(boolean deep);
}
