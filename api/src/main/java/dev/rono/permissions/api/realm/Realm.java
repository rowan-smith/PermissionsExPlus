package dev.rono.permissions.api.realm;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.permission.PermissionHolder;
import java.util.List;

/**
 * Registered permission namespace (realm) with inheritance metadata.
 *
 * <p>A realm is a logical scoping key for permissions and inheritance — a loaded Bukkit world, a
 * proxy backend server id, or another named namespace stored in the backend. Use
 * {@link PermissionContext} for day-to-day scoped checks and mutations; use {@link Realm} for
 * registry and inheritance administration.</p>
 */
public interface Realm {

    /**
     * Returns the stable realm identifier.
     *
     * @return realm name
     */
    String getName();

    /**
     * Returns the stable realm identifier.
     *
     * @return same value as {@link #getName()}
     */
    default String name() {
        return getName();
    }

    /**
     * Returns a {@link PermissionHolder} identity for holder-based permission operations.
     *
     * @return holder view of this realm
     */
    PermissionHolder asHolder();

    /**
     * Returns direct parent realms in the inheritance chain.
     *
     * @return immutable list of parent realm names (empty when none)
     */
    List<String> parents();

    /**
     * Replaces the direct parent realms for this namespace.
     *
     * @param parentNames parent realm names (must not be {@code null})
     */
    void setParents(List<String> parentNames);

    /**
     * Adds a parent realm when not already present.
     *
     * @param parentName parent realm name
     */
    void addParent(String parentName);

    /**
     * Removes a direct parent realm.
     *
     * @param parentName parent realm name
     */
    void removeParent(String parentName);

    /**
     * Returns all ancestor realm names reachable through the inheritance graph (breadth-first).
     *
     * @return immutable ordered list of ancestor names (does not include this realm)
     */
    List<String> parentTree();

    /**
     * Returns a {@link PermissionContext} scoped to this realm's {@code world} attribute.
     *
     * <p>On proxies, prefer {@link PermissionContext#server(String)} when the realm id is a backend
     * server name.</p>
     *
     * @return context bound to this realm name
     */
    default PermissionContext context() {
        return PermissionContext.world(getName());
    }
}
