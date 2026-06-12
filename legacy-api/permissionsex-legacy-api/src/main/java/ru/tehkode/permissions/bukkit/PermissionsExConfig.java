package ru.tehkode.permissions.bukkit;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Classic PermissionsEx plugin configuration view.
 *
 * <p>Exposed by {@link ru.tehkode.permissions.PermissionManager#getConfiguration()} and mirrors the
 * top-level settings from {@code config.yml} (backend selection, debug flags, updater behavior, and
 * related options).</p>
 */
public interface PermissionsExConfig {

    /**
     * Returns whether network-wide permission events should be propagated across linked servers.
     *
     * @return {@code true} if net events are enabled
     */
    boolean useNetEvents();

    /**
     * Returns whether debug logging is enabled.
     *
     * @return {@code true} if debug mode is active
     */
    boolean isDebug();

    /**
     * Returns whether server operators ({@code op}) receive implicit permission grants.
     *
     * @return {@code true} if ops are allowed full access by default
     */
    boolean allowOps();

    /**
     * Returns whether groups added via {@code /pex user ... group add} are appended last in the
     * inheritance list.
     *
     * @return {@code true} to append new groups at the end of the parent list
     */
    boolean userAddGroupsLast();

    /**
     * Returns the configured default permission backend alias.
     *
     * @return backend alias (for example {@code "file"}); never {@code null}
     */
    String getDefaultBackend();

    /**
     * Returns whether player join/leave events should be logged.
     *
     * @return {@code true} if player activity logging is enabled
     */
    boolean shouldLogPlayers();

    /**
     * Returns whether user records should be created automatically for unknown players.
     *
     * @return {@code true} to create user records on first access
     */
    boolean createUserRecords();

    /**
     * Returns whether the default group should be written to storage on startup.
     *
     * @return {@code true} to persist the default group definition automatically
     */
    boolean saveDefaultGroup();

    /**
     * Returns whether the built-in update checker is enabled.
     *
     * @return {@code true} if update checks are performed
     */
    boolean updaterEnabled();

    /**
     * Returns whether updates should be applied automatically without prompting.
     *
     * @return {@code true} to always apply available updates
     */
    boolean alwaysUpdate();

    /**
     * Returns whether online players should be notified about available plugin updates.
     *
     * @return {@code true} to inform players when an update is available
     */
    boolean informPlayers();

    /**
     * Returns server tags used for multi-server permission scoping.
     *
     * @return list of tag strings; never {@code null}
     */
    List<String> getServerTags();

    /**
     * Returns the base directory used for PermissionsEx data files.
     *
     * @return absolute or plugin-relative base directory path; never {@code null}
     */
    String getBasedir();

    /**
     * Returns the configuration section for a named backend.
     *
     * @param backend backend alias (for example {@code "file"} or {@code "sql"})
     * @return backend-specific configuration section, or {@code null} if not configured
     */
    ConfigurationSection getBackendConfig(String backend);

    /**
     * Persists the current configuration to disk.
     */
    void save();
}
