package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.user.User;
import java.util.List;
import java.util.Set;

/** Context-scoped view of a {@link Group}, including hierarchy and membership operations. */
public interface GroupContext extends SubjectContext {

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

    List<String> childIdentifiers();

    List<String> childIdentifiers(boolean inherit);

    List<String> descendantIdentifiers();

    List<User> activeMembers();

    List<User> activeMembers(boolean inherit);
}
