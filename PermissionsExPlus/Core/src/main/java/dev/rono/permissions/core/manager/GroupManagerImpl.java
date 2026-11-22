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
import dev.rono.permissions.api.event.Event;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.ApiStatus.Internal;
import dev.rono.permissions.core.util.Immutables;

public final class GroupManagerImpl implements GroupManager {

    private final DataStore store;
    private final GroupCacheManagerImpl cache;
    private final GroupStorageManagerImpl storage;

    private final EventBusImpl events;
    private final int maxDepth;
    private final Executor executor;
    private final boolean shorthandExpansions;

    private UserManagerImpl users;
    private LadderManagerImpl ladders;

    /*
     * Users, groups and ladders share one lock (the group manager's), because
     * operations such as a group rename change all three in one transaction.
     */
    private volatile Object lock = this;

    public GroupManagerImpl(DataStore store, EventBusImpl events, int maxDepth) {
        this(store, events, maxDepth, Runnable::run, true);
    }

    public GroupManagerImpl(DataStore store, EventBusImpl events, int maxDepth, Executor executor) {
        this(store, events, maxDepth, executor, true);
    }

    public GroupManagerImpl(DataStore store, EventBusImpl events, int maxDepth, Executor executor, boolean shorthandExpansions) {
        this.store = Objects.requireNonNull(store, "store");
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

        if (users != null) {
            users.shareLock(lock);
        }

        if (ladders != null) {
            ladders.shareLock(lock);
        }
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
            synchronized (lock) {
                return Mutations.run(store, () -> {
                    var key = Identifiers.group(name);

                    if (storage.getNow(key).isPresent()) {
                        throw new GroupAlreadyExistsException(key);
                    }

                    var group = new GroupSnapshot(key, OptionalInt.empty(), Immutables.setOf(), Immutables.setOf(), Immutables.setOf());

                    save(group);

                    publish(new GroupCreatedEventImpl(group));

                    return group;
                });
            }
        }, executor);
    }

    /**
     * Renames a group and every reference to it, in groups, users and ladders,
     * as one transaction: either all of it happens or none of it does.
     */
    @Internal
    public CompletionStage<Group> rename(String currentName, String newName) {
        return Stages.call(() -> {
            synchronized (lock) {
                return Mutations.run(store, () -> renameNow(currentName, newName));
            }
        }, executor);
    }

    private GroupSnapshot renameNow(String currentName, String newName) {
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
        uncache(oldKey);

        for (var group : storage.allNow().values()) {
            if (group.parents().stream().anyMatch(parent -> parent.group().equals(oldKey))) {
                modifyFromCurrent(group.name(), current -> modifier -> {
                    modifier.setParents(current.parents().stream().map(parent -> parent.group().equals(oldKey) ? ParentNode.builder(parent).group(newKey).build() : parent).collect(Immutables.toList()));
                });
            }
        }

        if (users != null) {
            users.renameGroup(oldKey, newKey);
        }

        if (ladders != null) {
            ladders.renameGroup(oldKey, newKey);
        }

        publish(new GroupModifiedEventImpl(previous, renamed));

        return renamed;
    }

    /**
     * Deletes a group and removes it from every group, user and ladder, as one
     * transaction.
     */
    @Internal
    public CompletionStage<Boolean> delete(String name) {
        return Stages.call(() -> {
            synchronized (lock) {
                return Mutations.run(store, () -> deleteNow(name));
            }
        }, executor);
    }

    private boolean deleteNow(String name) {
        String key = Identifiers.group(name);

        Optional<GroupSnapshot> previous = findNow(key);

        boolean changed = storage.deleteNow(key);

        if (!changed) {
            return false;
        }

        uncache(key);

        for (var group : storage.allNow().values()) {
            if (group.parents().stream().anyMatch(parent -> parent.group().equals(key))) {
                modifyFromCurrent(group.name(), current -> modifier -> {
                    modifier.setParents(current.parents().stream().filter(parent -> !parent.group().equals(key)).collect(Immutables.toList()));
                });
            }
        }

        if (users != null) {
            users.removeGroup(key);
        }

        if (ladders != null) {
            ladders.removeGroup(key);
        }

        previous.ifPresent(group -> publish(new GroupDeletedEventImpl(group)));

        return true;
    }

    @Internal
    public void loadAll() {
        synchronized (lock) {
            cache.clear();

            storage.allNow()
                    .values()
                    .stream()
                    .map(this::expand)
                    .forEach(cache::put);
        }
    }

    /**
     * Re-reads one group from storage after another server changed it, and
     * publishes the matching local event so that caches and online players are
     * refreshed.
     */
    @Internal
    public void refreshFromStorage(String name) {
        synchronized (lock) {
            var key = Identifiers.group(name);
            var previous = cache.get(key).map(GroupSnapshot.class::cast);
            var current = storage.getNow(key).map(this::expand);

            if (current.isPresent()) {
                cache.put(current.get());

                events.publish(previous.isPresent()
                        ? new GroupModifiedEventImpl(previous.get(), current.get())
                        : new GroupCreatedEventImpl(current.get()));
            } else if (previous.isPresent()) {
                cache.remove(key);

                events.publish(new GroupDeletedEventImpl(previous.get()));
            }
        }
    }

    @Internal
    public List<ExpiryRemoval> purgeExpired() {
        synchronized (lock) {
            var removals = new ArrayList<ExpiryRemoval>();

            for (var group : cache.snapshots()) {
                var expiredParents = group.parents().stream().filter(Node::expired).collect(Immutables.toList());
                var expiredPermissions = group.explicitPermissions().stream().filter(Node::expired).collect(Immutables.toList());
                var expiredOptions = group.explicitOptions().stream().filter(Node::expired).collect(Immutables.toList());

                expiredParents.forEach(node -> removals.add(new ExpiryRemoval("group", group.name(), "parent", node.group() + contextSuffix(node))));
                expiredPermissions.forEach(node -> removals.add(new ExpiryRemoval("group", group.name(), "permission", node.permission() + contextSuffix(node))));
                expiredOptions.forEach(node -> removals.add(new ExpiryRemoval("group", group.name(), "option", node.key() + '=' + node.value() + contextSuffix(node))));

                if (!expiredParents.isEmpty() || !expiredPermissions.isEmpty() || !expiredOptions.isEmpty()) {
                    modifyFromCurrent(group.name(), current -> modifier -> {
                        modifier.setParents(current.parents().stream().filter(node -> !node.expired()).collect(Immutables.toList()));

                        current.explicitPermissions().stream().filter(Node::expired).forEach(modifier::removePermission);
                        current.explicitOptions().stream().filter(Node::expired).forEach(modifier::removeOption);
                    });
                }
            }

            return Immutables.copyOfList(removals);
        }
    }

    public Optional<GroupSnapshot> findNow(String name) {
        synchronized (lock) {
            var key = Identifiers.group(name);
            var cached = cache.get(key).map(GroupSnapshot.class::cast);

            if (cached.isPresent()) {
                return cached;
            }

            return storage.getNow(key)
                    .map(this::expand)
                    .map(group -> {
                        cache(group);
                        return group;
                    });
        }
    }

    private GroupSnapshot loadNow(String name) {
        synchronized (lock) {
            var key = Identifiers.group(name);

            var group = storage.getNow(key)
                    .map(this::expand)
                    .orElseThrow(() -> new GroupNotFoundException(key));

            cache(group);

            return group;
        }
    }

    private GroupSnapshot modifyNow(String name, Consumer<GroupModifier> action) {
        Objects.requireNonNull(action, "action");

        return modifyFromCurrent(name, ignored -> action);
    }

    /**
     * Applies a change worked out from the group as stored now. A change that
     * loses a race with another server is worked out again from a fresh read.
     */
    private GroupSnapshot modifyFromCurrent(String name, Function<GroupSnapshot, Consumer<GroupModifier>> change) {
        synchronized (lock) {
            return Mutations.run(store, () -> {
                var previous = loadNow(name);
                var modifier = new GroupModifierImpl(previous);

                change.apply(previous).accept(modifier);

                var current = expand(modifier.build(previous));

                validate(current);
                save(current);

                publish(new GroupModifiedEventImpl(previous, current));

                return current;
            });
        }
    }

    private static String contextSuffix(Node node) {
        return node.contexts().isEmpty() ? "" : " " + node.contexts();
    }

    private void save(GroupSnapshot group) {
        GroupSnapshot expanded = expand(group);

        storage.saveNow(expanded);
        cache(expanded);
    }

    /** Caches a group; undone if the surrounding operation fails. */
    private void cache(GroupSnapshot group) {
        Mutations.onRollback(cache.restorer(group.name()));
        cache.put(group);
    }

    private void uncache(String key) {
        Mutations.onRollback(cache.restorer(key));
        cache.remove(key);
    }

    private void publish(Event event) {
        Mutations.afterCommit(() -> events.publish(event));
    }

    private GroupSnapshot expand(GroupSnapshot value) {
        if (!shorthandExpansions) {
            return value;
        }

        var permissions = value.explicitPermissions().stream().flatMap(node -> ShorthandCompiler
                .expand(node.permission()).stream().map(permission -> PermissionNode.builder(node).permission(permission).build()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return permissions.equals(value.explicitPermissions()) ? value : new GroupSnapshot(value.name(), value.weight(), permissions, value.explicitOptions(), value.parents(), value.revision());
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
