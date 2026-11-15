package dev.rono.permissions.core.modifier;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.group.GroupModifier;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.core.model.GroupSnapshot;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.OptionalInt;
import java.util.Set;

public final class GroupModifierImpl extends AbstractHolderModifier<GroupModifier> implements GroupModifier {
    private OptionalInt weight;
    private final Set<ParentNode> parents;

    public GroupModifierImpl(Group group) {
        super(group.explicitPermissions(), group.explicitOptions());

        weight = group.weight();
        parents = new LinkedHashSet<>(group.parents());
    }

    @Override
    protected GroupModifier self() {
        return this;
    }

    @Override
    public GroupModifier setPermission(PermissionNode node) {
        return setPermission0(node);
    }

    @Override
    public GroupModifier removePermission(String permission, ContextSet contexts) {
        return removePermission0(permission, contexts);
    }

    @Override
    public GroupModifier clearPermissions() {
        return clearPermissions0();
    }

    @Override
    public GroupModifier clearPermissions(ContextSet contexts) {
        return clearPermissions0(contexts);
    }

    @Override
    public GroupModifier setOption(OptionNode option) {
        return setOption0(option);
    }

    @Override
    public GroupModifier removeOption(String key, ContextSet contexts) {
        return removeOption0(key, contexts);
    }

    @Override
    public GroupModifier removeOptions(String key) {
        return removeOptions0(key);
    }

    @Override
    public GroupModifier clearOptions() {
        return clearOptions0();
    }

    @Override
    public GroupModifier clearOptions(ContextSet contexts) {
        return clearOptions0(contexts);
    }

    @Override
    public GroupModifier setWeight(int value) {
        weight = OptionalInt.of(value);
        return this;
    }

    @Override
    public GroupModifier clearWeight() {
        weight = OptionalInt.empty();
        return this;
    }

    @Override
    public GroupModifier addParent(ParentNode node) {
        parents.removeIf(value -> value.group().equals(node.group()) && value.contexts().equals(node.contexts()));
        parents.add(node);

        return this;
    }

    @Override
    public GroupModifier removeParent(String group, ContextSet contexts) {
        var key = Identifiers.group(group);

        parents.removeIf(value -> value.group().equals(key) && value.contexts().equals(contexts));

        return this;
    }

    @Override
    public GroupModifier clearParents() {
        parents.clear();
        return this;
    }

    @Override
    public GroupModifier setParents(Collection<ParentNode> values) {
        parents.clear();
        parents.addAll(values);

        return this;
    }

    public GroupSnapshot build(Group previous) {
        return new GroupSnapshot(previous.name(), weight, permissions, options, parents);
    }
}
