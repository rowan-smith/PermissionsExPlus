package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.RankingException;
import dev.rono.permissions.api.world.Worlds;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Modern view of a permission user. */
public interface User extends PermissionSubject {

    @Override
    default SubjectType type() {
        return SubjectType.USER;
    }

    /** Parsed UUID when the identifier is UUID-shaped; empty for name-only records. */
    Optional<UUID> uniqueId();

    @Override
    default UserWorldContext inWorld(String world) {
        return SubjectWorldContexts.user(this, world);
    }

    @Override
    default UserWorldContext global() {
        return inWorld(Worlds.GLOBAL);
    }

    /** Group identifiers the user inherits in {@code world} (includes parents of parents when {@code inherit} is true). */
    List<String> groups(String world, boolean inherit);

    default List<String> groups(String world) {
        return groups(world, true);
    }

    default List<String> groups() {
        return groups(Worlds.GLOBAL, true);
    }

    boolean inGroup(String groupName, String world, boolean inherit);

    default boolean inGroup(String groupName, String world) {
        return inGroup(groupName, world, true);
    }

    default boolean inGroup(String groupName) {
        return inGroup(groupName, Worlds.GLOBAL, true);
    }

    void addGroup(String groupName, String world);

    void addGroup(String groupName, String world, int lifetimeSeconds);

    default void addGroup(String groupName) {
        addGroup(groupName, Worlds.GLOBAL);
    }

    default void addGroup(String groupName, int lifetimeSeconds) {
        addGroup(groupName, Worlds.GLOBAL, lifetimeSeconds);
    }

    void removeGroup(String groupName, String world);

    default void removeGroup(String groupName) {
        removeGroup(groupName, Worlds.GLOBAL);
    }

    /** Timed group memberships in {@code world}. */
    List<TimedGroupMembership> timedGroupMemberships(String world);

    /** Timed group memberships across every configured world (including global). */
    default List<TimedGroupMembership> allTimedGroupMemberships() {
        java.util.LinkedHashSet<String> worlds = new java.util.LinkedHashSet<>();
        worlds.add(Worlds.GLOBAL);
        worlds.addAll(configuredWorlds());
        return worlds.stream().flatMap(world -> timedGroupMemberships(world).stream()).toList();
    }

    /** Seconds until timed membership expires; {@code 0} if not timed. */
    int groupMembershipRemainingSeconds(String groupName, String world);

    default int groupMembershipRemainingSeconds(String groupName) {
        return groupMembershipRemainingSeconds(groupName, Worlds.GLOBAL);
    }

    /** Promote one step on {@code ladderName}; {@code promoter} may be {@code null} (console/plugin). */
    Group promote(String ladderName) throws RankingException;

    Group promote(User promoter, String ladderName) throws RankingException;

    /** Demote one step on {@code ladderName}; {@code demoter} may be {@code null} (console/plugin). */
    Group demote(String ladderName) throws RankingException;

    Group demote(User demoter, String ladderName) throws RankingException;

    boolean isRanked(String ladderName);

    int rank(String ladderName);
}
