package dev.rono.permissions.core.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
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

    /**
     * The stored revision this snapshot was read at, or 0 when it was not read
     * from revision-checked storage. Not part of the snapshot's value.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    long revision;

    public UserSnapshot(UUID uniqueId, String name, Set<PermissionNode> explicitPermissions, Set<OptionNode> explicitOptions, Set<ParentNode> groups) {
        this(uniqueId, name, explicitPermissions, explicitOptions, groups, 0);
    }

    public UserSnapshot(UUID uniqueId, String name, Set<PermissionNode> explicitPermissions, Set<OptionNode> explicitOptions, Set<ParentNode> groups, long revision) {
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
        this.revision = revision;
    }
}
