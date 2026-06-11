package dev.rono.permissions.api.subject;

import java.util.List;

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
}
