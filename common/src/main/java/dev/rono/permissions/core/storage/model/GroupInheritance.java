package dev.rono.permissions.core.storage.model;

public final class GroupInheritance {

    private final int groupId;
    private final int parentId;

    public GroupInheritance(int groupId, int parentId) {
        this.groupId = groupId;
        this.parentId = parentId;
    }

    public int getGroupId() { return groupId; }
    public int getParentId() { return parentId; }
}
