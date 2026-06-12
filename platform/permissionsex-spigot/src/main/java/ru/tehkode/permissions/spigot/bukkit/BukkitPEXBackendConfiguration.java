package ru.tehkode.permissions.spigot.bukkit;

import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import ru.tehkode.permissions.PEXBackendConfiguration;

/**
 * Wraps Bukkit {@link ConfigurationSection} as {@link PEXBackendConfiguration} for core/backends.
 */
public final class BukkitPEXBackendConfiguration implements PEXBackendConfiguration {

    private final ConfigurationSection section;

    public BukkitPEXBackendConfiguration(ConfigurationSection section) {
        this.section = section;
    }

    public ConfigurationSection getConfigurationSection() {
        return section;
    }

    @Override
    public String getName() {
        return section.getName();
    }

    @Override
    public String getString(String path) {
        return section.getString(path);
    }

    @Override
    public String getString(String path, String def) {
        return section.getString(path, def);
    }

    @Override
    public void set(String path, Object value) {
        section.set(path, value);
    }

    @Override
    public List<String> getStringList(String path) {
        return section.getStringList(path);
    }

    @Override
    public PEXBackendConfiguration getConfigurationSection(String path) {
        ConfigurationSection sub = section.getConfigurationSection(path);
        return sub == null ? null : new BukkitPEXBackendConfiguration(sub);
    }

    @Override
    public PEXBackendConfiguration createSection(String path) {
        return new BukkitPEXBackendConfiguration(section.createSection(path));
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return section.isConfigurationSection(path);
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        return section.getValues(deep);
    }
}
