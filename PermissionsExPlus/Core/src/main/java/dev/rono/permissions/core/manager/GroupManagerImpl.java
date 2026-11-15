package dev.rono.permissions.core.manager;

import lombok.var;

import dev.rono.permissions.api.exception.GroupAlreadyExistsException;
import dev.rono.permissions.api.exception.GroupNotFoundException;
import dev.rono.permissions.api.exception.InheritanceCycleException;
import dev.rono.permissions.api.exception.InvalidParentException;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.group.GroupManager;
import dev.rono.permissions.api.group.GroupModifier;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.api.util.Node;
import dev.rono.permissions.core.event.EventBusImpl;
import dev.rono.permissions.core.event.GroupCreatedEventImpl;
import dev.rono.permissions.core.event.GroupDeletedEventImpl;
import dev.rono.permissions.core.event.GroupModifiedEventImpl;
import dev.rono.permissions.core.model.GroupSnapshot;
import dev.rono.permissions.core.modifier.GroupModifierImpl;
import dev.rono.permissions.core.store.DataStore;
import dev.rono.permissions.core.util.ShorthandCompiler;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jetbrains.annotations.ApiStatus.Internal;
import dev.rono.permissions.core.util.Immutables;

public final class GroupManagerImpl implements GroupManager {

    private final GroupCacheManagerImpl cache;
    private final GroupStorageManagerImpl storage;

    private final EventBusImpl events;
    private final int maxDepth;
    private final Executor executor;
    private final boolean shorthandExpansions;

    private UserManagerImpl users;
    private LadderManagerImpl ladders;

    public GroupManagerImpl(DataStore store, EventBusImpl events, int maxDepth) {
        this(store, events, maxDepth, Runnable::run, true);
    }

    public GroupManagerImpl(DataStore store, EventBusImpl events, int maxDepth, Executor executor) {
        this(store, events, maxDepth, executor, true);
    }

    public GroupManagerImpl(DataStore store, EventBusImpl events, int maxDepth, Executor executor, boolean shorthandExpansions) {
        this.events = Objects.requireNonNull(events, "events");
        this.maxDepth = Math.max(1, maxDepth);
        this.executor = Objects.requireNonNull(executor, "executor");
        this.shorthandExpansions = shorthandExpansions;

        this.cache = new GroupCacheManagerImpl();
        this.storage = new GroupStorageManagerImpl(store, executor);
    }

    @Internal
    public void attach(UserManagerImpl users, LadderManagerImpl ladders) {
        this.users = users;
        this.ladders = ladders;
    }

    @Override
    public GroupCacheManagerImpl cache() {
        return cache;
    }

    @Override
    public GroupStorageManagerImpl storage() {
        return storage;
    }

    @Override
    public CompletionStage<Group> modify(String name, Consumer<GroupModifier> action) {
        return Stages.call(() -> modifyNow(name, action), executor);
    }

    @Override
    public CompletionStage<Optional<Group>> find(String key) {
        return Stages.call(() -> findNow(key).map(Group.class::cast), executor);
    }

    @Internal
    public CompletionStage<Group> load(String key) {
        return Stages.call(() -> loadNow(key), executor);
    }

    @Internal
    public CompletionStage<Group> create(String name) {
        Objects.requireNonNull(name, "name");

        return Stages.call(() -> {
            synchronized (this) {
                var key = Identifiers.group(name);

                if (storage.getNow(key).isPresent()) {
                    throw new GroupAlreadyExistsException(key);
                }

                var group = new GroupSnapshot(key, OptionalInt.empty(), Immutables.setOf(), Immutables.setOf(), Immutables.setOf());

                save(group);

                events.publish(new GroupCreatedEventImpl(group));

                return group;
            }
        }, executor);
    }

    @Internal
    public CompletionStage<Group> rename(String currentName, String newName) {
        return Stages.call(() -> {
            synchronized (this) {
                String oldKey = Identifiers.group(currentName);
                String newKey = Identifiers.group(newName);

                var previous = loadNow(oldKey);

                if (storage.getNow(newKey).isPresent()) {
                    throw new GroupAlreadyExistsException(newKey);
                }

                var renamed = new GroupSnapshot(newKey, previous.weight(), previous.explicitPermissions(), previous.explicitOptions(), previous.parents());

                /*
                 * Save the replacement before removing the old entry so there is
                 * never a period where neither group exists.
                 */
                save(renamed);

                storage.deleteNow(oldKey);
                cache.remove(oldKey);

                for (var group : storage.allNow().values()) {
                    if (group.parents().stream().anyMatch(parent -> parent.group().equals(oldKey))) {
                        modifyNow(group.name(), modifier -> {
                            modifier.setParents(group.parents().stream().map(parent -> parent.group().equals(oldKey) ? ParentNode.builder(parent).group(newKey).build() : parent).collect(Immutables.toList()));
                        });
                    }
                }

                if (users != null) {
                    users.renameGroup(oldKey, newKey);
                }

                if (ladders != null) {
                    ladders.renameGroup(oldKey, newKey);
                }

                events.publish(new GroupModifiedEventImpl(previous, renamed));

                return renamed;
            }
        }, executor);
    }

    @Internal
    public CompletionStage<Boolean> delete(String name) {
        return Stages.call(() -> {
            synchronized (this) {
                String key = Identifiers.group(name);

                Optional<GroupSnapshot> previous = findNow(key);

                boolean changed = storage.deleteNow(key);

                if (!changed) {
                    return false;
                }

                cache.remove(key);

                for (var group : storage.allNow().values()) {
                    if (group.parents().stream().anyMatch(parent -> parent.group().equals(key))) {
                        modifyNow(group.name(), modifier -> {
                            modifier.setParents(group.parents().stream().filter(parent -> !parent.group().equals(key)).collect(Immutables.toList()));
                        });
                    }
                }

                if (users != null) {
                    users.removeGroup(key);
                }

                if (ladders != null) {
                    ladders.removeGroup(key);
                }

                previous.ifPresent(group -> events.publish(new GroupDeletedEventImpl(group)));

                return true;
            }
        }, executor);
    }

    @Internal
    public synchronized void loadAll() {
        cache.clear();

        storage.allNow()
                .values()
                .stream()
                .map(this::expand)
                .forEach(cache::put);
    }

    @Internal
    public synchronized List<ExpiryRemoval> purgeExpired() {
        var removals = new ArrayList<ExpiryRemoval>();

        for (var group : cache.snapshots()) {
            var expiredParents = group.parents().stream().filter(Node::expired).collect(Immutables.toList());
            var expiredPermissions = group.explicitPermissions().stream().filter(Node::expired).collect(Immutables.toList());
            var expiredOptions = group.explicitOptions().stream().filter(Node::expired).collect(Immutables.toList());

            expiredParents.forEach(node -> removals.add(new ExpiryRemoval("group", group.name(), "parent", node.group() + contextSuffix(node))));
            expiredPermissions.forEach(node -> removals.add(new ExpiryRemoval("group", group.name(), "permission", node.permission() + contextSuffix(node))));
            expiredOptions.forEach(node -> removals.add(new ExpiryRemoval("group", group.name(), "option", node.key() + '=' + node.value() + contextSuffix(node))));

            if (!expiredParents.isEmpty() || !expiredPermissions.isEmpty() || !expiredOptions.isEmpty()) {
                modifyNow(group.name(), modifier -> {
                    modifier.setParents(group.parents().stream().filter(node -> !node.expired()).collect(Immutables.toList()));

                    expiredPermissions.forEach(modifier::removePermission);
                    expiredOptions.forEach(modifier::removeOption);
                });
            }
        }

        return Immutables.copyOfList(removals);
    }

    public synchronized Optional<GroupSnapshot> findNow(String name) {
        var key = Identifiers.group(name);
        var cached = cache.get(key).map(GroupSnapshot.class::cast);

        if (cached.isPresent()) {
            return cached;
        }

        return storage.getNow(key)
                .map(this::expand)
                .map(group -> {
                    cache.put(group);
                    return group;
                });
    }

    private synchronized GroupSnapshot loadNow(String name) {
        var key = Identifiers.group(name);

        var group = storage.getNow(key)
                .map(this::expand)
                .orElseThrow(() -> new GroupNotFoundException(key));

        cache.put(group);

        return group;
    }

    private synchronized GroupSnapshot modifyNow(String name, Consumer<GroupModifier> action) {
        Objects.requireNonNull(action, "action");

        var previous = loadNow(name);
        var modifier = new GroupModifierImpl(previous);

        action.accept(modifier);

        var current = expand(modifier.build(previous));

        validate(current);
        save(current);

        events.publish(new GroupModifiedEventImpl(previous, current));

        return current;
    }

    private static String contextSuffix(Node node) {
        return node.contexts().isEmpty() ? "" : " " + node.contexts();
    }

    private void save(GroupSnapshot group) {
        GroupSnapshot expanded = expand(group);

        storage.saveNow(expanded);
        cache.put(expanded);
    }

    private GroupSnapshot expand(GroupSnapshot value) {
        if (!shorthandExpansions) {
            return value;
        }

        var permissions = value.explicitPermissions().stream().flatMap(node -> ShorthandCompiler
                .expand(node.permission()).stream().map(permission -> PermissionNode.builder(node).permission(permission).build()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return permissions.equals(value.explicitPermissions()) ? value : new GroupSnapshot(value.name(), value.weight(), permissions, value.explicitOptions(), value.parents());
    }

    private void validate(GroupSnapshot proposed) {
        for (var parent : proposed.parents()) {
            if (parent.group().equals(proposed.name())) {
                throw new InvalidParentException("A group cannot inherit itself: " + proposed.name());
            }

            if (!findNow(parent.group()).isPresent()) {
                throw new GroupNotFoundException(parent.group());
            }
        }

        detectCycle(proposed.name(), proposed, new LinkedHashSet<>(), 0);
    }

    private void detectCycle(String root, Group current, Set<String> path, int depth) {
        if (depth > maxDepth) {
            throw new InheritanceCycleException("Inheritance exceeds maximum depth of " + maxDepth);
        }

        if (!path.add(current.name())) {
            throw new InheritanceCycleException("Inheritance cycle: " + String.join(" -> ", path) + " -> " + current.name());
        }

        for (var parent : current.parents()) {
            if (parent.group().equals(root)) {
                throw new InheritanceCycleException("Inheritance cycle involving " + root);
            }

            findNow(parent.group()).ifPresent(next -> detectCycle(root, next, new LinkedHashSet<>(path), depth + 1));
        }
    }
}
