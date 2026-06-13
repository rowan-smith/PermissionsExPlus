package dev.rono.permissions.core.api;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.runtime.ContextResolver;
import dev.rono.permissions.api.runtime.PlatformAdapter;
import dev.rono.permissions.api.world.Worlds;
import ru.tehkode.permissions.PermissionEntity;

import java.util.Optional;

/** Resolves {@link PermissionContext} scopes through platform {@link ContextResolver} chains. */
public final class ContextPermissionEvaluator {

    private ContextPermissionEvaluator() {}

    /**
     * Checks whether {@code entity} holds {@code permission} across the platform inheritance chain.
     */
    public static boolean has(
            PermissionEntity entity, String permission, PermissionContext context, PlatformAdapter platform) {
        ContextResolver resolver = resolver(platform);
        for (PermissionContext step : resolver.inheritanceChain(context)) {
            String realm = toLegacyRealm(resolver.storageRealm(step));
            if (entity.has(permission, realm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the backend storage realm for persisting data at {@code context}.
     */
    public static String storageRealm(PermissionContext context, PlatformAdapter platform) {
        return toLegacyRealm(resolver(platform).storageRealm(context));
    }

    private static ContextResolver resolver(PlatformAdapter platform) {
        return platform != null ? platform.getContextResolver() : new dev.rono.permissions.api.runtime.BukkitContextResolver();
    }

    private static String toLegacyRealm(Optional<String> realm) {
        return realm.map(Worlds::normalize).orElse(null);
    }

    /** Converts legacy {@code String world} arguments to {@link PermissionContext}. */
    public static PermissionContext fromLegacyWorld(String world) {
        if (Worlds.isGlobal(world)) {
            return PermissionContext.global();
        }
        return PermissionContext.world(Worlds.normalize(world));
    }
}
