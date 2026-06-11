package ru.tehkode.permissions.bukkit;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Classic configuration view exposed by {@link ru.tehkode.permissions.PermissionManager#getConfiguration()}.
 */
public interface PermissionsExConfig {

    boolean useNetEvents();

    boolean isDebug();

    boolean allowOps();

    boolean userAddGroupsLast();

    String getDefaultBackend();

    boolean shouldLogPlayers();

    boolean createUserRecords();

    boolean saveDefaultGroup();

    boolean updaterEnabled();

    boolean alwaysUpdate();

    boolean informPlayers();

    List<String> getServerTags();

    String getBasedir();

    ConfigurationSection getBackendConfig(String backend);

    void save();
}
