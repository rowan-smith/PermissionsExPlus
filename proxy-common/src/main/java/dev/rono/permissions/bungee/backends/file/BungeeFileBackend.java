package dev.rono.permissions.bungee.backends.file;

import dev.rono.permissions.core.backends.file.YamlFileBackend;
import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
 * YAML-backed permissions store compatible with PermissionsEx classic {@code permissions.yml}.
 */
public final class BungeeFileBackend extends YamlFileBackend {

    public BungeeFileBackend(PermissionManager manager, PEXBackendConfiguration config)
            throws PermissionBackendException {
        super(manager, config);
    }
}
