package dev.rono.permissions.api.service;

/**
 * Read-only integration hooks for companion plugins. Register with the Bukkit {@code ServicesManager} under this type.
 */
public interface PermissionService {
    int registeredUserNameCount();

    int registeredGroupCount();

    String activeBackendSimpleName();
}
