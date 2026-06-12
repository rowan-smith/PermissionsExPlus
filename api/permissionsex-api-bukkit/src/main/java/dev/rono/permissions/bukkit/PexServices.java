package dev.rono.permissions.bukkit;

import dev.rono.permissions.api.service.PexPermissionService;

/**
 * Internal helper that resolves {@link PexPermissionService} from Bukkit {@code ServicesManager}.
 */
final class PexServices {

    private PexServices() {}

    /**
     * @return registered {@link PexPermissionService} provider
     * @throws IllegalStateException if PermissionsEx is not loaded or the service is not registered
     */
    static PexPermissionService require() {
        return PermissionsExPlus.getPermissionService();
    }
}
