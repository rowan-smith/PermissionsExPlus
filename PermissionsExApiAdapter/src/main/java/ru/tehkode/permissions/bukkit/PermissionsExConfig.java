package ru.tehkode.permissions.bukkit;

import dev.rono.permissions.api.PexProvider;
import dev.rono.permissions.core.PexImplProvider;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import ru.tehkode.permissions.backends.PermissionBackend;

/**
 * Compatibility facade for the legacy PermissionsEx configuration type.
 *
 * <p>
 * The adapter no longer owns a {@code config.yml}. Durable settings live in
 * PermissionsExPlus ({@code config.yml}, {@code advanced.yml},
 * {@code database.yml}). Public getters remain for binary compatibility.
 * </p>
 */
public class PermissionsExConfig {
    private final PermissionsEx plugin;

    public PermissionsExConfig(Configuration config, PermissionsEx plugin) {
        this.plugin = plugin;
    }

    /** Constructs a Plus-backed facade with no Bukkit configuration. */
    public PermissionsExConfig(PermissionsEx plugin) {
        this(new MemoryConfiguration(), plugin);
    }

    public boolean isDebug() {
        if (!PexProvider.available()) {
            return false;
        }

        return PexImplProvider.get().config().general().verboseDebug();
    }

    public boolean allowOps() {
        return false;
    }

    public boolean userAddGroupsLast() {
        return false;
    }

    public String getDefaultBackend() {
        return PermissionBackend.DEFAULT_BACKEND;
    }

    public boolean shouldLogPlayers() {
        return false;
    }

    public boolean createUserRecords() {
        return false;
    }

    public boolean saveDefaultGroup() {
        return false;
    }

    public boolean informPlayers() {
        return false;
    }

    public String getBasedir() {
        if (plugin != null) {
            return plugin.getDataFolder().getPath();
        }

        return "plugins/PermissionsEx";
    }

    public ConfigurationSection getBackendConfig(String backend) {
        ConfigurationSection section = new MemoryConfiguration().createSection(backend);
        section.set("type", backend != null && !backend.isEmpty() ? backend : PermissionBackend.DEFAULT_BACKEND);
        return section;
    }

    public void save() {
        // No-op: PermissionsExPlus owns configuration persistence.
    }
}
