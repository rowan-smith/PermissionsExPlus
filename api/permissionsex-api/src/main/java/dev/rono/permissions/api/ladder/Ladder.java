package dev.rono.permissions.api.ladder;

import dev.rono.permissions.api.permission.PermissionHolder;

/**
 * Rank ladder metadata (ordered promotion groups sharing a ladder name).
 *
 * <p>Promotion and demotion operations are on {@link LadderManager}; groups store rank state.</p>
 */
public interface Ladder {

    /**
     * Returns the ladder name.
     *
     * @return rank ladder identifier
     */
    String getName();

    /**
     * Returns a {@link PermissionHolder} identity for holder-based permission operations.
     *
     * @return holder view of this ladder
     */
    PermissionHolder asHolder();
}
