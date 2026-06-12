package ru.tehkode.permissions.spigot.backends;

import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
 * Legacy-visible in-memory backend alias.
 *
 * @see ru.tehkode.permissions.spigot.backends.memory.MemoryBackend
 */
public class MemoryBackend extends ru.tehkode.permissions.spigot.backends.memory.MemoryBackend {

    public MemoryBackend(PermissionManager manager, PEXBackendConfiguration config) throws PermissionBackendException {
        super(manager, config);
    }
}
