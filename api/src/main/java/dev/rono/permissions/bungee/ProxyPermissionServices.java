package dev.rono.permissions.bungee;

import dev.rono.permissions.api.PermissionsExApi;
import ru.tehkode.permissions.PermissionManager;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service registry for Bungee/Waterfall (no Bukkit {@code ServicesManager}).
 *
 * <p>Hook plugins should use {@link PermissionsEx#getApi()}. Legacy {@link PermissionManager}
 * registration is deferred until a hook plugin is detected or legacy entry points are used.</p>
 */
public final class ProxyPermissionServices {
    private static final AtomicReference<PermissionsExApi> PERMISSIONS_EX_API = new AtomicReference<>();
    private static final AtomicReference<PermissionManager> PERMISSION_MANAGER = new AtomicReference<>();

    private ProxyPermissionServices() {}

    public static void registerModern(PermissionsExApi api) {
        PERMISSIONS_EX_API.set(Objects.requireNonNull(api, "api"));
    }

    public static void activateLegacy(PermissionManager manager) {
        PERMISSION_MANAGER.set(Objects.requireNonNull(manager, "manager"));
    }

    /** Registers both modern and legacy surfaces (tests and forced compatibility). */
    public static void register(PermissionsExApi api, PermissionManager manager) {
        registerModern(api);
        activateLegacy(manager);
    }

    public static void unregister() {
        PERMISSIONS_EX_API.set(null);
        PERMISSION_MANAGER.set(null);
    }

    public static boolean isRegistered() {
        return PERMISSIONS_EX_API.get() != null;
    }

    public static boolean isLegacyActive() {
        return PERMISSION_MANAGER.get() != null;
    }

    public static PermissionsExApi permissionsExApi() {
        var api = PERMISSIONS_EX_API.get();
        if (api == null) {
            throw new IllegalStateException("PermissionsExApi is not registered on this proxy");
        }
        return api;
    }

    public static PermissionManager permissionManager() {
        var manager = PERMISSION_MANAGER.get();
        if (manager == null) {
            throw new IllegalStateException("PermissionManager is not registered on this proxy");
        }
        return manager;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) {
        Objects.requireNonNull(type, "type");
        if (PermissionsExApi.class.equals(type)) {
            return (T) permissionsExApi();
        }
        if (PermissionManager.class.equals(type)) {
            return (T) permissionManager();
        }
        throw new IllegalArgumentException("Unsupported service type on proxy: " + type.getName());
    }
}
