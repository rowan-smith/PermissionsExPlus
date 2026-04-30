package ru.tehkode.permissions;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Classic group node over {@link PermissionEntity} (implemented in core). */
public interface PermissionGroup extends PermissionEntity, Comparable<PermissionGroup> {

    int getWeight();

    void setWeight(int weight);

    boolean isRanked();

    int getRank();

    void setRank(int rank);

    String getRankLadder();

    void setRankLadder(String rankLadder);

    boolean isChildOf(PermissionGroup group, String worldName, boolean checkInheritance);

    boolean isChildOf(PermissionGroup group, boolean checkInheritance);

    boolean isChildOf(PermissionGroup group, String worldName);

    boolean isChildOf(PermissionGroup group);

    boolean isChildOf(String groupName, String worldName, boolean checkInheritance);

    boolean isChildOf(String groupName, boolean checkInheritance);

    boolean isChildOf(String groupName, String worldName);

    boolean isChildOf(String groupName);

    List<PermissionGroup> getChildGroups(String worldName);

    List<PermissionGroup> getChildGroups();

    List<PermissionGroup> getDescendantGroups(String worldName);

    List<PermissionGroup> getDescendantGroups();

    Set<PermissionUser> getUsers(String worldName);

    Set<PermissionUser> getUsers();

    Set<PermissionUser> getActiveUsers();

    Set<PermissionUser> getActiveUsers(boolean inheritance);

    boolean isDefault(String worldName);

    void setDefault(boolean def, String worldName);

    void addParent(PermissionGroup parent, String worldName);

    void addParent(PermissionGroup parent);

    void addParent(String parentName, String worldName);

    void addParent(String parentName);

    void removeParent(PermissionGroup parent, String worldName);

    void removeParent(PermissionGroup parent);

    void removeParent(String parentName, String worldName);

    void removeParent(String parentName);

    @Deprecated
    String[] getParentGroupsNames(String worldName);

    @Deprecated
    String[] getParentGroupsNames();

    @Deprecated
    void setParentGroups(List<String> parentGroups, String worldName);

    @Deprecated
    void setParentGroups(List<String> parentGroups);

    @Deprecated
    void setParentGroupObjects(List<PermissionGroup> parentGroups, String worldName);

    @Deprecated
    void setParentGroupObjects(List<PermissionGroup> parentGroups);

    @Deprecated
    List<PermissionGroup> getParentGroups(String worldName);

    @Deprecated
    List<PermissionGroup> getParentGroups();

    @Deprecated
    Map<String, List<PermissionGroup>> getAllParentGroups();
}
