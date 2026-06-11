package dev.rono.permissions.core.legacy;

import java.util.Collections;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import ru.tehkode.permissions.bukkit.PermissionsExConfig;

/**
 * Bridges {@link dev.rono.permissions.core.PermissionsExConfig} to the classic
 * {@link PermissionsExConfig} surface when the runtime config object does not implement it directly
 * (for example on Bungee).
 */
public final class LegacyPermissionsExConfigAdapter implements PermissionsExConfig {
    private final dev.rono.permissions.core.PermissionsExConfig config;

    public LegacyPermissionsExConfigAdapter(dev.rono.permissions.core.PermissionsExConfig config) {
        this.config = config;
    }

    @Override
    public boolean useNetEvents() {
        return false;
    }

    @Override
    public boolean isDebug() {
        return config.isDebug();
    }

    @Override
    public boolean allowOps() {
        return config.allowOps();
    }

    @Override
    public boolean userAddGroupsLast() {
        return config.userAddGroupsLast();
    }

    @Override
    public String getDefaultBackend() {
        return config.getDefaultBackend();
    }

    @Override
    public boolean shouldLogPlayers() {
        return config.shouldLogPlayers();
    }

    @Override
    public boolean createUserRecords() {
        return config.createUserRecords();
    }

    @Override
    public boolean saveDefaultGroup() {
        return config.saveDefaultGroup();
    }

    @Override
    public boolean updaterEnabled() {
        return false;
    }

    @Override
    public boolean alwaysUpdate() {
        return false;
    }

    @Override
    public boolean informPlayers() {
        return config.informPlayers();
    }

    @Override
    public List<String> getServerTags() {
        return Collections.emptyList();
    }

    @Override
    public String getBasedir() {
        return config.getBasedir();
    }

    @Override
    public ConfigurationSection getBackendConfig(String backend) {
        return null;
    }

    @Override
    public void save() {
        config.save();
    }
}
