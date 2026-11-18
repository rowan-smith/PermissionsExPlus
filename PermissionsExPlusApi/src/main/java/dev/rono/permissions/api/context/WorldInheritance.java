package dev.rono.permissions.api.context;

import java.util.List;
import java.util.Map;

/**
 * World inheritance, as in PermissionsEx: a world that inherits from another
 * also receives the permissions, options and group memberships set for that
 * world. With {@code world_nether} inheriting from {@code world}, a node set
 * with {@code world=world} also applies to a player in {@code world_nether}.
 *
 * <p>
 * Inheritance is transitive and applied to every query, so permission checks,
 * options, prefixes and group resolution all see it. It is configured per
 * server.
 */
public interface WorldInheritance {

    /** The worlds {@code world} inherits from directly, in order; empty if none. */
    List<String> parents(String world);

    /** Every configured world and its direct parents. */
    Map<String, List<String>> all();

    /**
     * Sets the worlds {@code world} inherits from, replacing any previous list,
     * and saves the change. An empty list removes the world's inheritance.
     */
    void set(String world, List<String> parents);

    /**
     * Adds every world inherited, directly or not, by the {@code world} values
     * in {@code contexts}. Other context keys are unchanged.
     */
    ContextSet expand(ContextSet contexts);
}
