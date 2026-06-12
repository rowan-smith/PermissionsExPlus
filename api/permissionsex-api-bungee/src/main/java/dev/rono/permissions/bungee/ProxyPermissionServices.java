package dev.rono.permissions.bungee;

import dev.rono.permissions.api.service.PexPermissionService;
import ru.tehkode.permissions.PermissionManager;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service registry for Bungee/Waterfall (no Bukkit {@code ServicesManager}).
 *
 * <p>PermissionsEx registers {@link PexPermissionService} and {@link PermissionManager} here on enable.
 * Hook plugins should prefer {@link PermissionsExPlus#getPermissionService()}.</p>
 */
public final class ProxyPermissionServices {
    private static final AtomicReference<PexPermissionService> PERMISSION_SERVICE = new AtomicReference<>();
    private static final AtomicReference<PermissionManager> PERMISSION_MANAGER = new AtomicReference<>();

    private ProxyPermissionServices() {}

    /**
     * Called by PermissionsEx on proxy enable.
     *
     * @param service modern permission service (same instance as {@code manager})
     * @param manager legacy permission manager facade
     */
    public static void register(PexPermissionService service, PermissionManager manager) {
        PERMISSION_SERVICE.set(Objects.requireNonNull(service, "service"));
        PERMISSION_MANAGER.set(Objects.requireNonNull(manager, "manager"));
    }

    /** Clears registered services on proxy disable. */
    public static void unregister() {
        PERMISSION_SERVICE.set(null);
        PERMISSION_MANAGER.set(null);
    }

    /**
     * Reports whether modern and legacy services are registered on this proxy.
     *
     * @return {@code true} when both services are available
     */
    public static boolean isRegistered() {
        return PERMISSION_SERVICE.get() != null && PERMISSION_MANAGER.get() != null;
    }

    public static PexPermissionService permissionService() {
        PexPermissionService service = PERMISSION_SERVICE.get();
        if (service == null) {
            throw new IllegalStateException("PexPermissionService is not registered on this proxy");
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
        if (PexPermissionService.class.equals(type)) {
            return (T) permissionService();
        }
        if (PermissionManager.class.equals(type)) {
            return (T) permissionManager();
        }
        throw new IllegalArgumentException("Unsupported service type on proxy: " + type.getName());
    }
}
