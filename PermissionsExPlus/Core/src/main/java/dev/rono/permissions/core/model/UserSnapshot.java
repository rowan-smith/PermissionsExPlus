package dev.rono.permissions.core.model;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.user.User;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import dev.rono.permissions.core.util.Immutables;

@Value
@Accessors(fluent = true)
public class UserSnapshot implements User {
    UUID uniqueId;
    String name;
    Set<PermissionNode> explicitPermissions;
    Set<OptionNode> explicitOptions;
    Set<ParentNode> groups;

    public UserSnapshot(UUID uniqueId, String name, Set<PermissionNode> explicitPermissions, Set<OptionNode> explicitOptions, Set<ParentNode> groups) {
        Objects.requireNonNull(uniqueId, "uniqueId");

        name = Objects.requireNonNull(name, "name").trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        explicitPermissions = Immutables.copyOfSet(explicitPermissions);
        explicitOptions = Immutables.copyOfSet(explicitOptions);

        groups = Immutables.copyOfSet(groups);

        this.uniqueId = uniqueId;
        this.name = name;
        this.explicitPermissions = explicitPermissions;
        this.explicitOptions = explicitOptions;
        this.groups = groups;
    }
}
