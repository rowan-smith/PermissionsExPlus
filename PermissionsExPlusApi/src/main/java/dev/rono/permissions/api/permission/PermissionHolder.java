package dev.rono.permissions.api.permission;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionKeys;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.util.Identifiers;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public interface PermissionHolder {

    /**
     * Returns the permissions explicitly assigned to this holder.
     *
     * <p>
     * The returned set is immutable and does not include inherited permissions.
     * </p>
     */
    Set<PermissionNode> explicitPermissions();

    Set<OptionNode> explicitOptions();

    default Optional<PermissionNode> explicitPermission(String permission) {
        return explicitPermission(permission, ContextSet.empty());
    }

    default Optional<PermissionNode> explicitPermission(String permission, ContextSet contexts) {
        var normalizedPermission = Identifiers.permission(permission);

        Objects.requireNonNull(contexts, "contexts");

        return explicitPermissions().stream()
                .filter(node -> node.permission().equals(normalizedPermission))
                .filter(node -> node.contexts().equals(contexts))
                .findFirst();
    }

    default boolean explicitlyAllows(String permission, ContextSet contexts) {
        return explicitPermission(permission, contexts)
                .map(PermissionNode::allowed)
                .orElse(false);
    }

    default boolean explicitlyDenies(String permission, ContextSet contexts) {
        return explicitPermission(permission, contexts)
                .map(PermissionNode::denied)
                .orElse(false);
    }

    default Optional<String> explicitPrefix() {
        return explicitOption(OptionKeys.PREFIX);
    }

    default Optional<String> explicitPrefix(ContextSet contexts) {
        return explicitOption(OptionKeys.PREFIX, contexts);
    }

    default Optional<String> explicitSuffix() {
        return explicitOption(OptionKeys.SUFFIX);
    }

    default Optional<String> explicitSuffix(ContextSet contexts) {
        return explicitOption(OptionKeys.SUFFIX, contexts);
    }

    default Optional<String> explicitOption(String key) {
        var normalizedKey = Identifiers.optionKey(key);

        return explicitOptions().stream()
                .filter(option -> option.key().equals(normalizedKey))
                .filter(option -> option.contexts().isEmpty())
                .map(OptionNode::value)
                .findFirst();
    }

    default Optional<String> explicitOption(String key, ContextSet contexts) {
        var normalizedKey = Identifiers.optionKey(key);

        Objects.requireNonNull(contexts, "contexts");

        return explicitOptions().stream()
                .filter(option -> option.key().equals(normalizedKey))
                .filter(option -> option.contexts().equals(contexts))
                .map(OptionNode::value)
                .findFirst();
    }
}
