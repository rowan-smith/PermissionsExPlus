package dev.rono.permissions.api.subject;

import java.util.List;

/** World-scoped view of a {@link User}. */
public interface UserWorldContext extends SubjectWorldContext {

    @Override
    User subject();

    List<String> groups(boolean inherit);

    default List<String> groups() {
        return groups(true);
    }

    boolean inGroup(String groupName, boolean inherit);

    default boolean inGroup(String groupName) {
        return inGroup(groupName, true);
    }

    void addGroup(String groupName);

    void addGroup(String groupName, int lifetimeSeconds);

    void removeGroup(String groupName);

    List<TimedGroupMembership> timedGroupMemberships();

    int groupMembershipRemainingSeconds(String groupName);
}
