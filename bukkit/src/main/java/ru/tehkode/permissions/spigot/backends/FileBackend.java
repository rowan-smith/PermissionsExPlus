package ru.tehkode.permissions.spigot.backends;

import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
 * Legacy-visible file backend alias (implements the classic {@code file} backend type).
 *
 * @see ru.tehkode.permissions.spigot.backends.file.FileBackend
 */
public class FileBackend extends ru.tehkode.permissions.spigot.backends.file.FileBackend {

    public FileBackend(PermissionManager manager, PEXBackendConfiguration config) throws PermissionBackendException {
        super(manager, config);
    }
}
