package dev.rono.permissions.api.world;

import java.util.Optional;
import java.util.function.Predicate;

/** World registry with explicit find/get/create/exists lifecycle. */
public interface WorldManager {

    Optional<World> findWorld(String name);

    World getWorld(String name) throws WorldNotFoundException;

    World createWorld(String name) throws WorldAlreadyExistsException;

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
