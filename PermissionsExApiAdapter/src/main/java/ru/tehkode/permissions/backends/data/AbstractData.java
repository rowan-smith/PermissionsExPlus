package ru.tehkode.permissions.backends.data;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionHolderModifier;
import dev.rono.permissions.api.permission.PermissionNode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import ru.tehkode.permissions.PermissionsData;
import dev.rono.permissions.core.util.Immutables;
import dev.rono.permissions.core.util.Strings;

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

    /**
     * Applies {@code action} to this holder through PermissionsExPlus and waits for
     * it.
     */
    protected abstract void modifyPermissions(Consumer<PermissionHolderModifier<?>> action);

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

        // Timed permissions are listed separately, as in PermissionsEx 1.23.6.
        return holder().explicitPermissions().stream().filter(node -> node.contexts().equals(contexts) && !node.expiry().isPresent())
                .map(PermissionNode::permission).collect(Immutables.toList());
    }

    /**
     * Unexpired timed permissions in {@code world}, with their expiry, in storage
     * order.
     */
    Map<String, Instant> getTimedPermissions(String world) {
        var contexts = contexts(world);
        var result = new LinkedHashMap<String, Instant>();

        holder().explicitPermissions().stream().filter(node -> node.contexts().equals(contexts) && node.expiry().isPresent() && !node.expired())
                .forEach(node -> result.put(node.permission(), node.expiry().get()));

        return Collections.unmodifiableMap(result);
    }

    /**
     * Unexpired timed permissions in every world; the global world is {@code ""}.
     */
    Map<String, Map<String, Instant>> getAllTimedPermissions() {
        var result = new LinkedHashMap<String, Map<String, Instant>>();

        holder().explicitPermissions().stream().filter(node -> node.expiry().isPresent() && !node.expired()).forEach(node -> {
            var world = world(node.contexts());
            result.computeIfAbsent(world == null ? "" : world, ignored -> new LinkedHashMap<>()).put(node.permission(), node.expiry().get());
        });

        return Collections.unmodifiableMap(result);
    }

    void addTimedPermission(String permission, String world, Instant expiry) {
        var node = PermissionNode.builder().permission(permission).contexts(contexts(world)).expiry(expiry).build();

        modifyPermissions(modifier -> modifier.setPermission(node));
    }

    /**
     * Removes {@code permission} from {@code world} if it is a timed permission
     * there.
     */
    boolean removeTimedPermission(String permission, String world) {
        if (!getTimedPermissions(world).containsKey(permission)) {
            return false;
        }

        var contexts = contexts(world);
        modifyPermissions(modifier -> modifier.removePermission(permission, contexts));
        return true;
    }

    /**
     * Replaces the permanent permissions in {@code contexts}, keeping timed ones.
     */
    protected static void replacePermanent(PermissionHolderModifier<?> modifier, PermissionHolder holder, ContextSet contexts, List<String> permissions) {
        holder.explicitPermissions().stream().filter(node -> node.contexts().equals(contexts) && !node.expiry().isPresent())
                .collect(Immutables.toList()).forEach(modifier::removePermission);
        permissions.forEach(value -> modifier.setPermission(PermissionNode.builder().permission(value).contexts(contexts).build()));
    }

    @Override
    public void setPermissions(List<String> permissions, String world) {
        replacePermissions(contexts(world), Immutables.copyOfList(permissions));
    }

    @Override
    public Map<String, List<String>> getPermissionsMap() {
        var result = new LinkedHashMap<String, List<String>>();

        for (var node : holder().explicitPermissions()) {
            if (!node.expiry().isPresent()) {
                result.computeIfAbsent(world(node.contexts()), ignored -> new ArrayList<>()).add(node.permission());
            }
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

            var values = new LinkedHashMap<>(result.getOrDefault(key, Immutables.mapOf()));
            values.put(node.key(), node.value());
            result.put(key, Collections.unmodifiableMap(values));
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public List<String> getParents(String world) {
        var contexts = contexts(world);

        return parents().stream().filter(node -> node.contexts().equals(contexts) && !node.expired())
                .map(ParentNode::group).collect(Immutables.toList());
    }

    @Override
    public void setParents(List<String> parents, String world) {
        replaceParents(contexts(world), Immutables.copyOfList(parents));
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
        return world == null || Strings.isBlank(world) ? ContextSet.empty() : ContextSet.builder().add("world", world).build();
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

        source.forEach((key, value) -> result.put(key, Immutables.copyOfList(value)));

        return Collections.unmodifiableMap(result);
    }
}
