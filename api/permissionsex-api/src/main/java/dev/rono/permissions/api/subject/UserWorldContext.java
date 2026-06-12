package dev.rono.permissions.api.subject;

import java.util.List;

/**
 * World-scoped view of a {@link User}.
 *
 * <p>Every method applies to the bound world from {@link #world()} (see {@link SubjectWorldContext}).</p>
 */
public interface UserWorldContext extends SubjectWorldContext {

    /**
     * Returns the underlying user.
     *
     * @return the user this context wraps
     */
    @Override
    User subject();

    /**
     * Returns group identifiers the user inherits in this context's world.
     *
     * <p>Equivalent to {@link User#groups(String, boolean)} with {@link #world()}.</p>
     *
     * @param inherit when {@code true}, expand transitive group inheritance
     * @return list of group identifiers
     */
    List<String> groups(boolean inherit);

    /**
     * Returns group identifiers the user inherits in this context's world, including transitive inheritance.
     *
     * <p>Delegates to {@link #groups(boolean)} with {@code inherit = true}.</p>
     *
     * @return list of group identifiers
     */
    default List<String> groups() {
        return groups(true);
    }

    /**
     * Returns whether the user belongs to the named group in this context's world.
     *
     * <p>Equivalent to {@link User#inGroup(String, String, boolean)} with {@link #world()}.</p>
     *
     * @param groupName group identifier to test
     * @param inherit   when {@code true}, match transitive group membership
     * @return {@code true} if the user is in the group, {@code false} otherwise
     */
    boolean inGroup(String groupName, boolean inherit);

    /**
     * Returns whether the user belongs to the named group in this context's world, including transitive membership.
     *
     * <p>Delegates to {@link #inGroup(String, boolean)} with {@code inherit = true}.</p>
     *
     * @param groupName group identifier to test
     * @return {@code true} if the user is in the group, {@code false} otherwise
     */
    default boolean inGroup(String groupName) {
        return inGroup(groupName, true);
    }

    /**
     * Adds the user to a group in this context's world.
     *
     * <p>Equivalent to {@link User#addGroup(String, String)} with {@link #world()}.</p>
     *
     * @param groupName group identifier to join
     */
    void addGroup(String groupName);

    /**
     * Adds the user to a group in this context's world with a timed membership.
     *
     * <p>Equivalent to {@link User#addGroup(String, String, int)} with {@link #world()}.</p>
     *
     * @param groupName       group identifier to join
     * @param lifetimeSeconds seconds until membership expires; {@code 0} for transient (in-memory only)
     */
    void addGroup(String groupName, int lifetimeSeconds);

    /**
     * Removes the user from a group in this context's world.
     *
     * <p>Equivalent to {@link User#removeGroup(String, String)} with {@link #world()}.</p>
     *
     * @param groupName group identifier to leave
     */
    void removeGroup(String groupName);

    /**
     * Returns timed group memberships in this context's world.
     *
     * <p>Equivalent to {@link User#timedGroupMemberships(String)} with {@link #world()}.</p>
     *
     * @return list of timed group membership entries
     */
    List<TimedGroupMembership> timedGroupMemberships();

    /**
     * Returns seconds until a timed group membership expires in this context's world.
     *
     * <p>Equivalent to {@link User#groupMembershipRemainingSeconds(String, String)} with {@link #world()}.</p>
     *
     * @param groupName group identifier
     * @return seconds until expiry; {@code 0} if membership is not timed
     */
    int groupMembershipRemainingSeconds(String groupName);
}
