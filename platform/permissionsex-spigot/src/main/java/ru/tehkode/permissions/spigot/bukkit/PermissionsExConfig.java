package ru.tehkode.permissions.spigot.bukkit;

import dev.rono.permissions.core.config.PexConfigData;
import dev.rono.permissions.core.config.PexConfigFlavor;
import dev.rono.permissions.core.config.PexRef;
import dev.rono.permissions.core.configuration.PexYamlConfig;
import dev.rono.permissions.core.runtime.PexVolatileRef;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.backends.PermissionBackend;

import java.util.Map;

/**
 * @author zml2008
 */
public final class PermissionsExConfig implements dev.rono.permissions.core.PermissionsExConfig,
        ru.tehkode.permissions.bukkit.PermissionsExConfig {

    private final SpigotPermissionsExPlugin plugin;
    private PexVolatileRef<PexConfigData> loaded;

    public PermissionsExConfig(SpigotPermissionsExPlugin plugin) {
        this.plugin = plugin;
        ensureYamlDefaults();
        rebuildFromDisk();
    }

    /**
     * Mirrors legacy field-level defaults (mutates bukkit configuration in memory).
     */
    private void ensureYamlDefaults() {
        Configuration c = plugin.getConfig();
        setIfUnset(c, "permissions.debug", false);
        setIfUnset(c, "permissions.allowOps", false);
        setIfUnset(c, "permissions.user-add-groups-last", false);
        setIfUnset(c, "permissions.log-players", false);
        setIfUnset(c, "permissions.createUserRecords", false);
        setIfUnset(c, "permissions.save-default-group", false);
        setIfUnset(c, "permissions.backend", PermissionBackend.DEFAULT_BACKEND);
        setIfUnset(c, "permissions.basedir", "plugins/" + plugin.getDescription().getName());

        ConfigurationSection inform = c.getConfigurationSection("permissions.informplayers");
        if (inform == null) {
            inform = c.createSection("permissions.informplayers");
        }
        if (!inform.isSet("changes")) {
            inform.set("changes", false);
        }
    }

    private static void setIfUnset(Configuration c, String path, Object def) {
        if (!c.isSet(path)) {
            c.set(path, def);
        }
    }

    /** Rebind from {@link org.bukkit.plugin.java.JavaPlugin#getConfig()} after disk reload. */
    public void refreshFromDisk() {
        ensureYamlDefaults();
        rebuildFromDisk();
    }

    private void rebuildFromDisk() {
        Map<String, Object> permissions = BukkitYamlMaps.permissionsSection(
                plugin.getConfig().getConfigurationSection("permissions"));
        try {
            dev.rono.permissions.core.config.PexConfigValidator.validatePermissionsSection(permissions);
        } catch (ru.tehkode.permissions.exceptions.PermissionBackendException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        PexConfigData snap =
                PexYamlConfig.load(
                        permissions,
                        () -> "plugins/" + plugin.getDescription().getName(),
                        PexConfigFlavor.SPIGOT);
        if (loaded == null) {
            loaded = new PexVolatileRef<>(snap);
        } else {
            loaded.replace(snap);
        }
    }

    @Override
    public PexRef<PexConfigData> options() {
        return loaded;
    }

    @Override
    public void setDefaultBackend(String backendName) {
        plugin.getConfig().set("permissions.backend", backendName);
        rebuildFromDisk();
        plugin.saveConfig();
    }

    @Override
    public boolean isDebug() {
        return loaded.current().debug();
    }

    @Override
    public boolean allowOps() {
        return loaded.current().allowOps();
    }

    @Override
    public boolean userAddGroupsLast() {
        return loaded.current().userAddGroupsLast();
    }

    @Override
    public String getDefaultBackend() {
        return loaded.current().backend();
    }

    @Override
    public boolean shouldLogPlayers() {
        return loaded.current().logPlayers();
    }

    @Override
    public boolean createUserRecords() {
        return loaded.current().createUserRecords();
    }

    @Override
    public boolean saveDefaultGroup() {
        return loaded.current().saveDefaultGroup();
    }

    @Override
    public boolean informPlayers() {
        return loaded.current().informPlayerChanges();
    }

    @Override
    public String getBasedir() {
        return loaded.current().basedir();
    }

    @Override
    public boolean useNetEvents() {
        return false;
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
    public java.util.List<String> getServerTags() {
        return java.util.Collections.emptyList();
    }

    @Override
    public ConfigurationSection getBackendConfig(String backend) {
        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection("permissions.backends." + backend);
        if (section == null) {
            section = plugin.getConfig().createSection("permissions.backends." + backend);
        }
        return section;
    }

    @Override
    public PEXBackendConfiguration pexBackendConfiguration(String backend) {
        return new BukkitPEXBackendConfiguration(getBackendConfig(backend));
    }

    @Override
    public void save() {
        plugin.saveConfig();
    }
}
