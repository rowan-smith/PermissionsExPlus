package ru.tehkode.permissions.backends;

import org.bukkit.configuration.ConfigurationSection;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
 * Legacy {@code multi} backend alias.
 *
 * <p>
 * Composite legacy stores are no longer supported. This type remains for binary
 * compatibility and redirects to the PermissionsExPlus data bridge.
 * </p>
 */
public class MultiBackend extends ru.tehkode.permissions.backends.data.PermissionBackend {
    public MultiBackend(PermissionManager manager, ConfigurationSection backendConfig) throws PermissionBackendException {
        super(manager, backendConfig);
        manager.getLogger().info("multi backend alias redirects to PermissionsExPlus data storage");
    }
}
