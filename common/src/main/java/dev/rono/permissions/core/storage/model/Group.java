package dev.rono.permissions.core.storage.model;

import java.util.List;

public final class Group {

    private final int id;
    private final String name;
    private final int weight;
    private final boolean defaultGroup;
    private final List<GroupPermission> permissions;
    private final List<GroupInheritance> parents;
    private final GroupOptions options;

    public Group(int id,
                 String name,
                 int weight,
                 boolean defaultGroup,
                 List<GroupPermission> permissions,
                 List<GroupInheritance> parents,
                 GroupOptions options) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.defaultGroup = defaultGroup;
        this.permissions = List.copyOf(permissions);
        this.parents = List.copyOf(parents);
        this.options = options;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getWeight() { return weight; }
    public boolean isDefaultGroup() { return defaultGroup; }
    public List<GroupPermission> getPermissions() { return permissions; }
    public List<GroupInheritance> getParents() { return parents; }
    public GroupOptions getOptions() { return options; }
}
