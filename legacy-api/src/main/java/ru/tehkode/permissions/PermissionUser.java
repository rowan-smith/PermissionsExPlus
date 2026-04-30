package ru.tehkode.permissions;

import java.util.List;
import java.util.Map;

import ru.tehkode.permissions.exceptions.RankingException;

/** Classic online-subject facade over {@link PermissionEntity} (implemented in core). */
public interface PermissionUser extends PermissionEntity {

    @Deprecated
    Map<String, List<PermissionGroup>> getAllGroups();

    void addGroup(String groupName, String worldName);

    void addGroup(String groupName);

    void addGroup(PermissionGroup group, String worldName);

    void addGroup(PermissionGroup group);

    void addGroup(String groupName, String worldName, long lifetime);

    void removeGroup(String groupName, String worldName);

    void removeGroup(String groupName);

    void removeGroup(PermissionGroup group, String worldName);

    void removeGroup(PermissionGroup group);

    boolean inGroup(PermissionGroup group, String worldName, boolean checkInheritance);

    boolean inGroup(PermissionGroup group, boolean checkInheritance);

    boolean inGroup(String groupName, String worldName, boolean checkInheritance);

    boolean inGroup(String groupName, boolean checkInheritance);

    boolean inGroup(PermissionGroup group, String worldName);

    boolean inGroup(PermissionGroup group);

    boolean inGroup(String groupName, String worldName);

    boolean inGroup(String groupName);

    PermissionGroup promote(PermissionUser promoter, String ladderName) throws RankingException;

    PermissionGroup demote(PermissionUser demoter, String ladderName) throws RankingException;

    boolean isRanked(String ladder);

    int getRank(String ladder);

    PermissionGroup getRankLadderGroup(String ladder);

    Map<String, PermissionGroup> getRankLadders();

    void updateTimedGroups();

    @Deprecated
    String[] getGroupsNames();

    @Deprecated
    String[] getGroupsNames(String world);

    @Deprecated
    PermissionGroup[] getGroups();

    @Deprecated
    PermissionGroup[] getGroups(String worldName);

    @Deprecated
    String[] getGroupNames();

    @Deprecated
    String[] getGroupNames(String worldName);

    @Deprecated
    void setGroups(String[] groups, String worldName);

    @Deprecated
    void setGroups(String[] groups);

    @Deprecated
    void setGroups(PermissionGroup[] parentGroups, String worldName);

    @Deprecated
    void setGroups(PermissionGroup[] parentGroups);
}
