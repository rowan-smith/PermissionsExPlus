package dev.rono.permissions.bukkit;

import dev.rono.permissions.api.service.PermissionService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Internal helper that resolves {@link PermissionService} from Bukkit {@code ServicesManager}.
 */
final class PexServices {

    private PexServices() {}

    /**
     * @return registered {@link PermissionService} provider
     * @throws IllegalStateException if PermissionsEx is not loaded or the service is not registered
     */
    static PermissionService require() {
        RegisteredServiceProvider<PermissionService> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionService.class);
        if (reg == null) {
            throw new IllegalStateException("PermissionService is not registered — is PermissionsEx loaded?");
        }
        return reg.getProvider();
    }
}
