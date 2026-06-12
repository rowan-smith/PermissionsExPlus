package dev.rono.permissions.api.subject;

import java.util.List;
import java.util.Set;

/** World-scoped view of a {@link Group}. */
public interface GroupWorldContext extends SubjectWorldContext {

    @Override
    Group subject();

    boolean isDefault();

    void setDefault(boolean value);

    List<String> parents();

    List<String> parentTree();

    void addParent(String parentName);

    void removeParent(String parentName);

    void setParents(List<String> parentNames);

    boolean isChildOf(String groupName, boolean inherit);

    default boolean isChildOf(String groupName) {
        return isChildOf(groupName, true);
    }

    Set<String> memberIdentifiers();

    List<User> members();

    List<User> members(boolean inherit);

    List<Group> children();

    List<Group> children(boolean inherit);

    List<Group> descendants();

    List<User> activeMembers();

    List<User> activeMembers(boolean inherit);
}
