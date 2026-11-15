package dev.rono.permissions.api.user;

import lombok.var;

import dev.rono.permissions.api.context.ContextKeys;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.NamedPermissionHolder;
import dev.rono.permissions.api.util.Identifiers;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A UUID-identified user whose current display name preserves its supplied
 * case.
 */
public interface User extends NamedPermissionHolder {

    /**
     * Retrieves the unique identifier of the user.
     *
     * @return the UUID representing the unique identifier of the user
     */
    UUID uniqueId();

    Set<ParentNode> groups();

    default boolean hasDirectGroup(String group, ContextSet contexts) {
        var normalizedGroup = Identifiers.group(group);

        Objects.requireNonNull(contexts, "contexts");

        return groups().stream()
                .anyMatch(node -> node.group().equals(normalizedGroup) && node.contexts().equals(contexts));
    }

    default boolean hasDirectGroup(String group, ContextKeys contextKey, String contextValue) {
        return hasDirectGroup(group, ContextSet.builder().add(contextKey, contextValue).build());
    }
}
