package dev.rono.permissions.bungee;

import dev.rono.permissions.api.service.PermissionService;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import ru.tehkode.permissions.PermissionManager;

/**
 * Service registry for Bungee/Waterfall (no Bukkit {@code ServicesManager}).
 *
 * <p>PermissionsEx registers {@link PermissionService} and {@link PermissionManager} here on enable.</p>
 */
public final class ProxyPermissionServices {
    private static final AtomicReference<PermissionService> PERMISSION_SERVICE = new AtomicReference<>();
    private static final AtomicReference<PermissionManager> PERMISSION_MANAGER = new AtomicReference<>();

    private ProxyPermissionServices() {}

    /**
     * Called by PermissionsEx on proxy enable.
     *
     * @param service modern permission service (same instance as {@code manager})
     * @param manager legacy permission manager facade
     */
    public static void register(PermissionService service, PermissionManager manager) {
        PERMISSION_SERVICE.set(Objects.requireNonNull(service, "service"));
        PERMISSION_MANAGER.set(Objects.requireNonNull(manager, "manager"));
    }

    /** Clears registered services on proxy disable. */
    public static void unregister() {
        PERMISSION_SERVICE.set(null);
        PERMISSION_MANAGER.set(null);
    }

    public static PermissionService permissionService() {
        PermissionService service = PERMISSION_SERVICE.get();
        if (service == null) {
            throw new IllegalStateException("PermissionService is not registered on this proxy");
        }
        return service;
    }

    public static PermissionManager permissionManager() {
        PermissionManager manager = PERMISSION_MANAGER.get();
        if (manager == null) {
            throw new IllegalStateException("PermissionManager is not registered on this proxy");
        }
        return manager;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) {
        Objects.requireNonNull(type, "type");
        if (PermissionService.class.equals(type)) {
            return (T) permissionService();
        }
        if (PermissionManager.class.equals(type)) {
            return (T) permissionManager();
        }
        throw new IllegalArgumentException("Unsupported service type on proxy: " + type.getName());
    }
}
