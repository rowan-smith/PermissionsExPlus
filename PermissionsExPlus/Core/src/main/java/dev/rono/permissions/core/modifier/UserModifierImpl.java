package dev.rono.permissions.core.modifier;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.user.UserModifier;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.core.model.UserSnapshot;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class UserModifierImpl extends AbstractHolderModifier<UserModifier> implements UserModifier {
    private final Set<ParentNode> groups;
    private String username;

    public UserModifierImpl(User user) {
        super(user.explicitPermissions(), user.explicitOptions());

        groups = new LinkedHashSet<>(user.groups());
        username = user.name();
    }

    @Override
    protected UserModifier self() {
        return this;
    }

    @Override
    public UserModifier setPermission(PermissionNode node) {
        return setPermission0(node);
    }

    @Override
    public UserModifier removePermission(String permission, ContextSet contexts) {
        return removePermission0(permission, contexts);
    }

    @Override
    public UserModifier clearPermissions() {
        return clearPermissions0();
    }

    @Override
    public UserModifier clearPermissions(ContextSet contexts) {
        return clearPermissions0(contexts);
    }

    @Override
    public UserModifier setOption(OptionNode option) {
        return setOption0(option);
    }

    @Override
    public UserModifier removeOption(String key, ContextSet contexts) {
        return removeOption0(key, contexts);
    }

    @Override
    public UserModifier removeOptions(String key) {
        return removeOptions0(key);
    }

    @Override
    public UserModifier clearOptions() {
        return clearOptions0();
    }

    @Override
    public UserModifier clearOptions(ContextSet contexts) {
        return clearOptions0(contexts);
    }

    @Override
    public UserModifier updateName(String username) {
        this.username = username;
        return this;
    }

    @Override
    public UserModifier addGroup(ParentNode node) {
        groups.removeIf(value -> value.group().equals(node.group()) && value.contexts().equals(node.contexts()));
        groups.add(node);

        return this;
    }

    @Override
    public UserModifier removeGroup(String group, ContextSet contexts) {
        var key = Identifiers.group(group);

        groups.removeIf(value -> value.group().equals(key) && value.contexts().equals(contexts));

        return this;
    }

    @Override
    public UserModifier clearGroups() {
        groups.clear();
        return this;
    }

    @Override
    public UserModifier setGroups(Collection<ParentNode> values) {
        groups.clear();
        groups.addAll(values);

        return this;
    }

    public UserSnapshot build(User previous) {
        return new UserSnapshot(previous.uniqueId(), username, permissions, options, groups, previous instanceof UserSnapshot ? ((UserSnapshot) previous).revision() : 0);
    }
}
