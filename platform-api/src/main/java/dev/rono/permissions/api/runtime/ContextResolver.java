package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.permission.PermissionContext;

import java.util.List;
import java.util.Optional;

/**
 * Platform-specific permission scope resolution and inheritance ordering.
 *
 * <p>Each host (Paper, proxy, Sponge, …) supplies a resolver through {@link PlatformAdapter#getContextResolver()}.
 * The engine never assumes a world dimension exists.</p>
 */
public interface ContextResolver {

    /** @return server attribute when present on {@code context} */
    Optional<String> server(PermissionContext context);

    /** @return world attribute when present on {@code context} */
    Optional<String> world(PermissionContext context);

    /** @return dimension attribute when present on {@code context} */
    Optional<String> dimension(PermissionContext context);

    /**
     * Returns ordered contexts from most specific to global for permission checks.
     *
     * @param context active check scope
     * @return immutable inheritance chain ending in {@link PermissionContext#global()}
     */
    List<PermissionContext> inheritanceChain(PermissionContext context);

    /**
     * Returns the backend storage namespace when persisting data for {@code context}.
     *
     * <p>Defaults to the most specific realm known to this platform (dimension, then world, then server).</p>
     *
     * @param context write scope
     * @return storage realm slug, or empty for global
     */
    default Optional<String> storageRealm(PermissionContext context) {
        return dimension(context).or(() -> world(context)).or(() -> server(context));
    }
}
