package dev.rono.permissions.api.world;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * World registry with explicit find/get/create/exists lifecycle.
 *
 * <p>Worlds are permission namespaces. {@link #count()} may include backend inheritance entries
 * that do not correspond to a loaded server dimension.</p>
 */
public interface WorldManager {

    /**
     * Looks up a registered world without creating a record.
     *
     * @param name world name
     * @return the world when registered; empty if absent
     */
    Optional<World> findWorld(String name);

    /**
     * Returns a registered world by name.
     *
     * @param name world name
     * @return live world adapter
     * @throws WorldNotFoundException if {@code name} is not registered
     */
    World getWorld(String name) throws WorldNotFoundException;

    /**
     * Registers a new world namespace.
     *
     * @param name world name
     * @return live world adapter for the new record
     * @throws WorldAlreadyExistsException if {@code name} is already registered
     */
    World createWorld(String name) throws WorldAlreadyExistsException;

    /**
     * Reports whether a world is registered.
     *
     * @param name world name
     * @return {@code true} if the world exists in the registry
     */
    boolean exists(String name);

    /**
     * Returns the number of registered worlds.
     *
     * <p>Includes both platform-registered realms ({@link World}) and backend world-inheritance
     * entries that may not correspond to a loaded dimension. Do not assume every counted world is
     * a live server world — use platform APIs to test whether a dimension is loaded.</p>
     *
     * @return total registered world count
     */
    int count();

    /**
     * Returns how many registered worlds match {@code filter}.
     *
     * @param filter predicate applied to each registered world; must not be {@code null}
     * @return count of worlds for which the predicate is {@code true}
     */
    int count(Predicate<World> filter);
}
