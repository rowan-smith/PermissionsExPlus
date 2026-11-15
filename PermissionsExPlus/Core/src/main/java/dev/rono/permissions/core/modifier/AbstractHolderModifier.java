package dev.rono.permissions.core.modifier;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionModifier;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.permission.PermissionHolderModifier;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.util.Identifiers;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

abstract class AbstractHolderModifier<S extends PermissionHolderModifier<S> & OptionModifier<S>> {
    protected final Set<PermissionNode> permissions;

    protected final Set<OptionNode> options;

    AbstractHolderModifier(Collection<PermissionNode> permissions, Collection<OptionNode> options) {
        this.permissions = new LinkedHashSet<>(permissions);

        this.options = new LinkedHashSet<>(options);
    }

    protected abstract S self();

    public S setPermission0(PermissionNode node) {
        Objects.requireNonNull(node, "node");

        permissions.removeIf(value -> value.permission().equals(node.permission()) && value.contexts().equals(node.contexts()));
        permissions.add(node);

        return self();
    }

    public S removePermission0(String permission, ContextSet contexts) {
        var key = Identifiers.permission(permission);
        Objects.requireNonNull(contexts, "contexts");

        permissions.removeIf(value -> value.permission().equals(key) && value.contexts().equals(contexts));

        return self();
    }

    public S clearPermissions0() {
        permissions.clear();

        return self();
    }

    public S clearPermissions0(ContextSet contexts) {
        permissions.removeIf(node -> node.contexts().equals(contexts));

        return self();
    }

    public S setOption0(OptionNode node) {
        Objects.requireNonNull(node, "node");

        options.removeIf(value -> value.key().equals(node.key()) && value.contexts().equals(node.contexts()));
        options.add(node);

        return self();
    }

    public S removeOption0(String key, ContextSet contexts) {
        var normalized = Identifiers.optionKey(key);
        Objects.requireNonNull(contexts, "contexts");

        options.removeIf(value -> value.key().equals(normalized) && value.contexts().equals(contexts));

        return self();
    }

    public S removeOptions0(String key) {
        var normalized = Identifiers.optionKey(key);

        options.removeIf(value -> value.key().equals(normalized));

        return self();
    }

    public S clearOptions0() {
        options.clear();

        return self();
    }

    public S clearOptions0(ContextSet contexts) {
        options.removeIf(node -> node.contexts().equals(contexts));

        return self();
    }
}
