package dev.rono.permissions.core.backends;

import dev.rono.permissions.core.backends.sql.SQLBackend;

import ru.tehkode.permissions.backends.PermissionBackend;
/**
 * Registers backends that ship in core and apply to every platform adapter (Spigot, Bungee, …).
 */
public final class CorePermissionBackendRegistrar {
    private CorePermissionBackendRegistrar() {}

    private static volatile boolean installed;

    public static void ensureRegistered() {
        if (installed) {
            return;
        }
        synchronized (CorePermissionBackendRegistrar.class) {
            if (installed) {
                return;
            }
            PermissionBackend.registerBackendAlias("sql", SQLBackend.class);
            PermissionBackend.registerBackendAlias("multi", MultiBackend.class);
            installed = true;
        }
    }
}
