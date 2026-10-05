package ru.tehkode.permissions.backends.data;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ru.tehkode.permissions.PermissionsData;

abstract class AbstractData implements PermissionsData {
    protected String identifier;

    AbstractData(String identifier) {
        this.identifier = Objects.requireNonNull(identifier, "identifier");
    }

    protected abstract PermissionHolder holder();

    protected abstract Set<ParentNode> parents();

    protected abstract void replacePermissions(ContextSet contexts, List<String> permissions);

    protected abstract void replaceParents(ContextSet contexts, List<String> parents);

    protected abstract void setOptionNode(ContextSet contexts, String key, String value);

    @Override
    public void load() {
        holder();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public List<String> getPermissions(String world) {
        var contexts = contexts(world);

        return holder().explicitPermissions().stream().filter(node -> node.contexts().equals(contexts))
                .map(PermissionNode::permission).toList();
    }

    @Override
    public void setPermissions(List<String> permissions, String world) {
        replacePermissions(contexts(world), List.copyOf(permissions));
    }

    @Override
    public Map<String, List<String>> getPermissionsMap() {
        var result = new LinkedHashMap<String, List<String>>();

        for (var node : holder().explicitPermissions()) {
            result.computeIfAbsent(world(node.contexts()), ignored -> new ArrayList<>()).add(node.permission());
        }

        return immutableLists(result);
    }

    @Override
    public Set<String> getWorlds() {
        var result = new LinkedHashSet<String>();

        holder().explicitPermissions().forEach(node -> addWorld(result, node.contexts()));
        holder().explicitOptions().forEach(node -> addWorld(result, node.contexts()));
        parents().forEach(node -> addWorld(result, node.contexts()));

        return Collections.unmodifiableSet(result);
    }

    @Override
    public String getOption(String option, String world) {
        var contexts = contexts(world);

        return holder()
                .explicitOptions().stream().filter(node -> node.key().equalsIgnoreCase(option)
                        && node.contexts().equals(contexts) && !node.expired())
                .map(OptionNode::value).findFirst().orElse(null);
    }

    @Override
    public void setOption(String option, String value, String world) {
        setOptionNode(contexts(world), option, value);
    }

    @Override
    public Map<String, String> getOptions(String world) {
        var contexts = contexts(world);
        var result = new LinkedHashMap<String, String>();

        holder().explicitOptions().stream().filter(node -> node.contexts().equals(contexts) && !node.expired())
                .forEach(node -> result.put(node.key(), node.value()));

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<String, Map<String, String>> getOptionsMap() {
        var result = new LinkedHashMap<String, Map<String, String>>();

        for (var node : holder().explicitOptions()) {
            var key = world(node.contexts());

            var values = new LinkedHashMap<>(result.getOrDefault(key, Map.of()));
            values.put(node.key(), node.value());
            result.put(key, Collections.unmodifiableMap(values));
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public List<String> getParents(String world) {
        var contexts = contexts(world);

        return parents().stream().filter(node -> node.contexts().equals(contexts) && !node.expired())
                .map(ParentNode::group).toList();
    }

    @Override
    public void setParents(List<String> parents, String world) {
        replaceParents(contexts(world), List.copyOf(parents));
    }

    @Override
    public Map<String, List<String>> getParentsMap() {
        var result = new LinkedHashMap<String, List<String>>();

        for (var node : parents()) {
            result.computeIfAbsent(world(node.contexts()), ignored -> new ArrayList<>()).add(node.group());
        }

        return immutableLists(result);
    }

    @Override
    public void save() {
        holder();
    }

    protected static ContextSet contexts(String world) {
        return world == null || world.isBlank() ? ContextSet.empty() : ContextSet.builder().add("world", world).build();
    }

    private static String world(ContextSet contexts) {
        return contexts.values("world").stream().findFirst().orElse(null);
    }

    private static void addWorld(Set<String> worlds, ContextSet contexts) {
        var value = world(contexts);

        if (value != null) {
            worlds.add(value);
        }
    }

    private static Map<String, List<String>> immutableLists(Map<String, List<String>> source) {
        var result = new LinkedHashMap<String, List<String>>();

        source.forEach((key, value) -> result.put(key, List.copyOf(value)));

        return Collections.unmodifiableMap(result);
    }
}
