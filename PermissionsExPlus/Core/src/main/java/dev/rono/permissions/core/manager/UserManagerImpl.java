package dev.rono.permissions.core.manager;

import lombok.var;

import dev.rono.permissions.api.exception.GroupNotFoundException;
import dev.rono.permissions.api.exception.UserAlreadyExistsException;
import dev.rono.permissions.api.exception.UserNotFoundException;
import dev.rono.permissions.api.group.GroupManager;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.user.UserManager;
import dev.rono.permissions.api.user.UserModifier;
import dev.rono.permissions.api.util.Node;
import dev.rono.permissions.core.event.EventBusImpl;
import dev.rono.permissions.core.event.UserCreatedEventImpl;
import dev.rono.permissions.core.event.UserDeletedEventImpl;
import dev.rono.permissions.core.event.UserModifiedEventImpl;
import dev.rono.permissions.core.model.UserSnapshot;
import dev.rono.permissions.core.modifier.UserModifierImpl;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import dev.rono.permissions.core.store.DataStore;
import dev.rono.permissions.core.util.ShorthandCompiler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jetbrains.annotations.ApiStatus.Internal;
import dev.rono.permissions.core.util.Immutables;

public final class UserManagerImpl implements UserManager {

    private final UserCacheManagerImpl cache;
    private final UserStorageManagerImpl storage;
    private final EventBusImpl events;
    private final Executor executor;
    private final boolean shorthandExpansions;

    private GroupManager groups;
    private PlatformScheduler scheduler;
    private PlatformLogger logger;

    private final Set<UUID> pendingUserLoads = ConcurrentHashMap.newKeySet();

    public UserManagerImpl(DataStore store, EventBusImpl events) {
        this(store, events, Duration.ofMinutes(10), 5000, Runnable::run, true);
    }

    public UserManagerImpl(
            DataStore store,
            EventBusImpl events,
            Duration offlineExpiry,
            int maximumOfflineUsers) {

        this(store, events, offlineExpiry, maximumOfflineUsers, Runnable::run, true);
    }

    public UserManagerImpl(
            DataStore store,
            EventBusImpl events,
            Duration offlineExpiry,
            int maximumOfflineUsers,
            Executor executor) {

        this(store, events, offlineExpiry, maximumOfflineUsers, executor, true);
    }

    public UserManagerImpl(
            DataStore store,
            EventBusImpl events,
            Duration offlineExpiry,
            int maximumOfflineUsers,
            Executor executor,
            boolean shorthandExpansions) {

        this.events = Objects.requireNonNull(events, "events");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.shorthandExpansions = shorthandExpansions;

        this.cache = new UserCacheManagerImpl(offlineExpiry, maximumOfflineUsers);
        this.storage = new UserStorageManagerImpl(store, executor);
    }

    public UserManagerImpl(
            DataStore store,
            EventBusImpl events,
            Duration offlineExpiry,
            int maximumOfflineUsers,
            Executor executor,
            boolean shorthandExpansions,
            PlatformScheduler scheduler,
            PlatformLogger logger) {

        this(store, events, offlineExpiry, maximumOfflineUsers, executor, shorthandExpansions);

        this.scheduler = scheduler;
        this.logger = logger;
    }

    @Internal
    public void attachGroups(GroupManager groups) {
        this.groups = Objects.requireNonNull(groups, "groups");
    }

    @Override
    public UserCacheManagerImpl cache() {
        return cache;
    }

    @Override
    public UserStorageManagerImpl storage() {
        return storage;
    }

    @Override
    public CompletionStage<User> modify(UUID id, Consumer<UserModifier> action) {
        Objects.requireNonNull(action, "action");

        return Stages.call(() -> {
            synchronized (this) {
                var previous = loadNow(id);

                var modifier = new UserModifierImpl(previous);

                action.accept(modifier);

                var current = expand(modifier.build(previous));

                validate(current);

                save(current);

                events.publish(new UserModifiedEventImpl(previous, current));

                return current;
            }
        }, executor);
    }

    @Override
    public CompletionStage<User> modify(String username, Consumer<UserModifier> action) {
        return Stages.call(() -> modifyNow(findNameStored(username).orElseThrow(() -> new UserNotFoundException(username)).uniqueId(), action), executor);
    }

    @Override
    public CompletionStage<Optional<User>> find(UUID id) {
        return Stages.call(() -> findNow(id).map(User.class::cast), executor);
    }

    @Override
    public CompletionStage<Optional<User>> find(String username) {
        return Stages.call(() -> findNameNow(username).map(User.class::cast), executor);
    }

    @Internal
    public CompletionStage<Boolean> delete(UUID id) {
        return Stages.call(() -> {
            synchronized (this) {
                var previous = findNow(id);

                var changed = storage.deleteNow(id);
                if (!changed) {
                    return false;
                }

                cache.remove(id);

                previous.ifPresent(user -> {
                    events.publish(new UserDeletedEventImpl(user));
                });

                return true;
            }
        }, executor);
    }

    @Internal
    public CompletionStage<User> create(UUID id) {
        return create(id, id.toString());
    }

    @Internal
    public CompletionStage<User> create(UUID id, String username) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(username, "username");

        return Stages.call(() -> {
            synchronized (this) {
                if (storage.getNow(id).isPresent() || storage.getNow(username).isPresent()) {
                    throw new UserAlreadyExistsException(username);
                }

                var user = new UserSnapshot(id, username, Immutables.setOf(), Immutables.setOf(), Immutables.setOf());

                save(user);
                events.publish(new UserCreatedEventImpl(user));

                return user;
            }
        }, executor);
    }

    @Internal
    public CompletionStage<User> load(UUID id) {
        return Stages.call(() -> loadNow(id), executor);
    }

    @Internal
    public CompletionStage<User> loadOrCreateUser(UUID id, String name) {
        return Stages.call(() -> {
            synchronized (this) {
                var existing = findNow(id);
                if (existing.isPresent()) {
                    return existing.get();
                }

                existing = findNameStored(name);
                if (existing.isPresent()) {
                    return existing.get();
                }

                var user = new UserSnapshot(id, name, Immutables.setOf(), Immutables.setOf(), Immutables.setOf());
                save(user);
                events.publish(new UserCreatedEventImpl(user));
                return user;
            }
        }, executor);
    }

    @Internal
    public void loadOrCreateUserAsync(UUID id, String name, Consumer<User> callback) {
        if (!pendingUserLoads.add(id)) {
            return;
        }

        loadOrCreateUser(id, name).whenComplete((user, error) -> pendingUserLoads.remove(id))
                .whenComplete((user, error) -> {
                    if (error != null) {
                        if (logger != null) {
                            logger.error("Unable to load user " + id, error);
                        }
                    } else if (scheduler != null) {
                        scheduler.execute(() -> callback.accept(user));
                    } else {
                        callback.accept(user);
                    }
                });
    }

    @Internal
    public void createUserAsync(UUID id, String name, BiConsumer<User, Boolean> callback) {
        var task = (Runnable) () -> find(id).whenComplete((existing, lookupError) -> {
            if (lookupError != null) {
                if (logger != null) {
                    logger.error("Unable to find user " + id, lookupError);
                }

                return;
            }

            boolean created = !existing.isPresent();

            var stage = existing.<CompletionStage<User>>map(CompletableFuture::completedFuture).orElseGet(() -> create(id, name));

            stage.whenComplete((user, error) -> {
                if (error != null) {
                    if (logger != null) {
                        logger.error("Unable to create user " + id, error);
                    }
                } else if (scheduler != null) {
                    scheduler.execute(() -> callback.accept(user, created));
                } else {
                    callback.accept(user, created);
                }
            });
        });

        if (scheduler != null) {
            scheduler.executeAsync(task);
        } else {
            task.run();
        }
    }

    @Internal
    public synchronized List<ExpiryRemoval> purgeExpired() {
        var removals = new ArrayList<ExpiryRemoval>();

        for (var user : loadedSnapshots()) {
            var subject = user.name() + " (" + user.uniqueId() + ")";

            var expiredGroups = user.groups().stream().filter(Node::expired).collect(Immutables.toList());
            var expiredPermissions = user.explicitPermissions().stream().filter(Node::expired).collect(Immutables.toList());
            var expiredOptions = user.explicitOptions().stream().filter(Node::expired).collect(Immutables.toList());

            expiredGroups.forEach(node -> removals.add(new ExpiryRemoval("user", subject, "group membership", node.group() + contextSuffix(node))));
            expiredPermissions.forEach(node -> removals.add(new ExpiryRemoval("user", subject, "permission", node.permission() + contextSuffix(node))));
            expiredOptions.forEach(node -> removals.add(new ExpiryRemoval("user", subject, "option", node.key() + '=' + node.value() + contextSuffix(node))));

            if (!expiredGroups.isEmpty() || !expiredPermissions.isEmpty() || !expiredOptions.isEmpty()) {
                modifyNow(user.uniqueId(), modifier -> {
                    modifier.setGroups(user.groups().stream().filter(node -> !node.expired()).collect(Immutables.toList()));

                    expiredPermissions.forEach(modifier::removePermission);

                    expiredOptions.forEach(modifier::removeOption);
                });
            }
        }

        return Immutables.copyOfList(removals);
    }

    @Internal
    synchronized void renameGroup(String oldName, String newName) {
        for (var user : storage.allNow().values()) {
            if (user.groups().stream().anyMatch(parent -> parent.group().equals(oldName))) {

                modifyNow(
                        user.uniqueId(),
                        modifier -> modifier.setGroups(
                                user.groups().stream()
                                        .map(parent -> parent.group().equals(oldName)
                                                ? ParentNode.builder(parent)
                                                        .group(newName)
                                                        .build()
                                                : parent)
                                        .collect(Immutables.toList())));
            }
        }
    }

    @Internal
    synchronized void removeGroup(String name) {
        for (var user : storage.allNow().values()) {
            if (user.groups().stream().anyMatch(parent -> parent.group().equals(name))) {
                modifyNow(user.uniqueId(), modifier -> {
                    modifier.setGroups(user.groups().stream()
                            .filter(parent -> !parent.group().equals(name))
                            .collect(Immutables.toList()));
                });
            }
        }
    }

    private synchronized Optional<UserSnapshot> findNow(UUID id) {
        var cached = cache.get(id);

        if (cached.isPresent()) {
            return cached.map(UserSnapshot.class::cast);
        }

        return storage.getNow(id)
                .map(this::expand)
                .map(user -> {
                    cache.put(user);

                    return user;
                });
    }

    private synchronized UserSnapshot loadNow(UUID id) {
        var user = storage.getNow(id)
                .map(this::expand)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));

        cache.put(user);

        return user;
    }

    private synchronized Optional<UserSnapshot> findNameNow(String name) {
        var cached = cache.get(name).map(UserSnapshot.class::cast);

        if (cached.isPresent()) {
            return cached;
        }

        return findNameStored(name);
    }

    private synchronized Optional<UserSnapshot> findNameStored(String name) {
        return storage.getNow(name)
                .map(this::expand)
                .map(user -> {
                    cache.put(user);
                    return user;
                });
    }

    private synchronized User modifyNow(UUID id, Consumer<UserModifier> action) {
        var previous = loadNow(id);

        var modifier = new UserModifierImpl(previous);

        action.accept(modifier);

        var current = expand(modifier.build(previous));

        validate(current);

        save(current);

        events.publish(new UserModifiedEventImpl(previous, current));

        return current;
    }

    private void validate(User user) {
        if (groups == null) {
            return;
        }

        for (var membership : user.groups()) {
            if (!groups.cache().get(membership.group()).isPresent()) {
                throw new GroupNotFoundException(membership.group());
            }
        }
    }

    private void save(UserSnapshot user) {
        var expanded = expand(user);

        storage.saveNow(expanded);
        cache.put(expanded);
    }

    private UserSnapshot expand(UserSnapshot user) {
        if (!shorthandExpansions) {
            return user;
        }

        var permissions = user.explicitPermissions().stream().flatMap(node -> ShorthandCompiler
                .expand(node.permission()).stream().map(permission -> PermissionNode.builder(node)
                        .permission(permission).build()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return permissions.equals(user.explicitPermissions())
                ? user
                : new UserSnapshot(user.uniqueId(), user.name(), permissions, user.explicitOptions(), user.groups());
    }

    private List<UserSnapshot> loadedSnapshots() {
        return cache.all().stream()
                .map(UserSnapshot.class::cast)
                .collect(Immutables.toList());
    }

    private static String contextSuffix(Node node) {
        return node.contexts().isEmpty() ? "" : " " + node.contexts();
    }
}
