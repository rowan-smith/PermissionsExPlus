package dev.rono.permissions.api.group;

import lombok.var;

import dev.rono.permissions.api.context.ContextKeys;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.NamedPermissionHolder;
import dev.rono.permissions.api.util.Identifiers;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;

/**
 * A group whose name is a lowercase-normalized, case-insensitive identifier.
 */
public interface Group extends NamedPermissionHolder {

    OptionalInt weight();

    /**
     * Returns the names of groups directly inherited by this group.
     *
     * <p>
     * The returned set is immutable and does not include indirect
     * ancestors.
     * </p>
     *
     * @return a set of parent group names directly inherited by this group
     */
    Set<ParentNode> parents();

    default boolean hasDirectParent(ParentNode parent) {
        Objects.requireNonNull(parent, "parent");

        return parents().contains(parent);
    }

    default boolean hasDirectParent(String group, ContextSet contexts) {
        var normalizedGroup = Identifiers.group(group);

        Objects.requireNonNull(contexts, "contexts");

        return parents().stream()
                .anyMatch(node -> node.group().equals(normalizedGroup) && node.contexts().equals(contexts));
    }

    default boolean hasDirectParent(String group, ContextKeys contextKey, String contextValue) {
        return hasDirectParent(group, ContextSet.builder().add(contextKey, contextValue).build());
    }
}
