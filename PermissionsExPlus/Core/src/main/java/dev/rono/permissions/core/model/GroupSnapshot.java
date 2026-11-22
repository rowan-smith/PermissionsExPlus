package dev.rono.permissions.core.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.util.Identifiers;

import java.util.OptionalInt;
import java.util.Set;
import dev.rono.permissions.core.util.Immutables;

@Value
@Accessors(fluent = true)
public class GroupSnapshot implements Group {
    String name;
    OptionalInt weight;
    Set<PermissionNode> explicitPermissions;
    Set<OptionNode> explicitOptions;
    Set<ParentNode> parents;

    /**
     * The stored revision this snapshot was read at, or 0 when it was not read
     * from revision-checked storage. Not part of the snapshot's value.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    long revision;

    public GroupSnapshot(String name, OptionalInt weight, Set<PermissionNode> explicitPermissions, Set<OptionNode> explicitOptions, Set<ParentNode> parents) {
        this(name, weight, explicitPermissions, explicitOptions, parents, 0);
    }

    public GroupSnapshot(String name, OptionalInt weight, Set<PermissionNode> explicitPermissions, Set<OptionNode> explicitOptions, Set<ParentNode> parents, long revision) {
        name = Identifiers.group(name);
        weight = weight == null ? OptionalInt.empty() : weight;
        explicitPermissions = Immutables.copyOfSet(explicitPermissions);
        explicitOptions = Immutables.copyOfSet(explicitOptions);
        parents = Immutables.copyOfSet(parents);

        this.name = name;
        this.weight = weight;
        this.explicitPermissions = explicitPermissions;
        this.explicitOptions = explicitOptions;
        this.parents = parents;
        this.revision = revision;
    }
}
