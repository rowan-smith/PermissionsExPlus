package dev.rono.permissions.core.model;

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

    public GroupSnapshot(String name, OptionalInt weight, Set<PermissionNode> explicitPermissions, Set<OptionNode> explicitOptions, Set<ParentNode> parents) {
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
    }
}
