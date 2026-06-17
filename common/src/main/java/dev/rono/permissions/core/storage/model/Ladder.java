package dev.rono.permissions.core.storage.model;

import java.util.List;

public final class Ladder {

    private final int id;
    private final String name;
    private final List<LadderGroup> groups;

    public Ladder(int id, String name, List<LadderGroup> groups) {
        this.id = id;
        this.name = name;
        this.groups = List.copyOf(groups);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public List<LadderGroup> getGroups() { return groups; }
}
