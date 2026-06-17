package dev.rono.permissions.core.storage.model;

public final class LadderGroup {

    private final int ladderId;
    private final int groupId;
    private final int position;

    public LadderGroup(int ladderId, int groupId, int position) {
        this.ladderId = ladderId;
        this.groupId = groupId;
        this.position = position;
    }

    public int getLadderId() { return ladderId; }
    public int getGroupId() { return groupId; }
    public int getPosition() { return position; }
}
