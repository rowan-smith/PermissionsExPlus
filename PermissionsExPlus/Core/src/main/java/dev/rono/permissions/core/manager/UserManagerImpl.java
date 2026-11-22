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
import dev.rono.permissions.api.event.Event;
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
import java.nio.charset.StandardCharsets;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.ApiStatus.Internal;
import dev.rono.permissions.core.util.Immutables;

public final class UserManagerImpl implements UserManager {

    private final DataStore store;
    private final UserCacheManagerImpl cache;
    private final UserStorageManagerImpl storage;
    private final EventBusImpl events;
    private final Executor executor;
    private final boolean shorthandExpansions;

    private GroupManager groups;
    private PlatformScheduler scheduler;
    private PlatformLogger logger;

    private final Set<UUID> pendingUserLoads = ConcurrentHashMap.newKeySet();

    /*
     * Users, groups and ladders share one lock (the group manager's), because
     * operations such as a group rename change all three in one transaction.
     */
    private volatile Object lock = this;

    /**
     * Uses the group manager's lock, so cross-manager operations cannot deadlock.
     */
    void shareLock(Object lock) {
        this.lock = lock;
    }

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

        this.store = Objects.requireNonNull(store, "store");
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

        return Stages.call(() -> modifyNow(id, action), executor);
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
            synchronized (lock) {
                return Mutations.run(store, () -> {
                    var previous = findNow(id);

                    var changed = storage.deleteNow(id);
                    if (!changed) {
                        return false;
                    }

                    uncache(id);

                    previous.ifPresent(user -> publish(new UserDeletedEventImpl(user)));

                    return true;
                });
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
            synchronized (lock) {
                return Mutations.run(store, () -> {
                    if (storage.getNow(id).isPresent() || storage.getNow(username).isPresent()) {
                        throw new UserAlreadyExistsException(username);
                    }

                    var user = new UserSnapshot(id, username, Immutables.setOf(), Immutables.setOf(), Immutables.setOf());

                    save(user);
                    publish(new UserCreatedEventImpl(user));

                    return user;
                });
            }
        }, executor);
    }

    @Internal
    public CompletionStage<User> load(UUID id) {
        return Stages.call(() -> loadNow(id), executor);
    }

    /**
     * Re-reads a cached user from storage after another server changed it, and
     * publishes the matching local event so that caches and the player are
     * refreshed. Users not cached here are read from storage when next needed.
     */
    @Internal
    public void refreshFromStorage(UUID id) {
        synchronized (lock) {
            var previous = cache.get(id).map(UserSnapshot.class::cast);

            if (!previous.isPresent()) {
                return;
            }

            var current = storage.getNow(id).map(this::expand);

            if (current.isPresent()) {
                cache.put(current.get());

                events.publish(new UserModifiedEventImpl(previous.get(), current.get()));
            } else {
                cache.remove(id);

                events.publish(new UserDeletedEventImpl(previous.get()));
            }
        }
    }

    @Internal
    public CompletionStage<User> loadOrCreateUser(UUID id, String name) {
        return Stages.call(() -> {
            synchronized (lock) {
                return Mutations.run(store, () -> {
                    var existing = findNow(id);

                    if (existing.isPresent() && existing.get().name().equals(name)) {
                        return existing.get();
                    }

                    // Names are unique but do not identify a player: never hand
                    // back the account of a different UUID.
                    var sameName = findNameStored(name).filter(user -> !user.uniqueId().equals(id));

                    if (existing.isPresent()) {
                        return refreshName(existing.get(), sameName, name);
                    }

                    if (sameName.isPresent() && isOfflinePlaceholder(sameName.get())) {
                        return adoptPlaceholder(sameName.get(), id, name);
                    }

                    sameName.ifPresent(this::releaseName);

                    var user = new UserSnapshot(id, name, Immutables.setOf(), Immutables.setOf(), Immutables.setOf());
                    save(user);
                    publish(new UserCreatedEventImpl(user));
                    return user;
                });
            }
        }, executor);
    }

    /**
     * Stores the name a known player now uses. Anyone else still holding that
     * name gives it up first.
     */
    private UserSnapshot refreshName(UserSnapshot user, Optional<UserSnapshot> sameName, String name) {
        sameName.ifPresent(this::releaseName);

        if (logger != null) {
            logger.info("User " + user.uniqueId() + " is now called " + name + " (was " + user.name() + ")");
        }

        return (UserSnapshot) modifyNow(user.uniqueId(), modifier -> modifier.updateName(name));
    }

    /**
     * Whether a user was added by name before the player joined, under the
     * offline-mode UUID for that name, as PermissionsEx 1.x commands allowed.
     */
    private static boolean isOfflinePlaceholder(UserSnapshot user) {
        var offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + user.name()).getBytes(StandardCharsets.UTF_8));

        return user.uniqueId().equals(offline);
    }

    /** Moves a placeholder's data to the joining player's real UUID. */
    private UserSnapshot adoptPlaceholder(UserSnapshot placeholder, UUID id, String name) {
        // The name is released first because names are unique in storage, and the
        // placeholder is only removed once the replacement is saved.
        storage.saveNow(new UserSnapshot(placeholder.uniqueId(), placeholder.uniqueId().toString(), placeholder.explicitPermissions(), placeholder.explicitOptions(), placeholder.groups(), placeholder.revision()));

        var user = new UserSnapshot(id, name, placeholder.explicitPermissions(), placeholder.explicitOptions(), placeholder.groups());
        save(user);

        storage.deleteNow(placeholder.uniqueId());
        uncache(placeholder.uniqueId());

        if (logger != null) {
            logger.info("Moved user " + placeholder.name() + " from offline UUID " + placeholder.uniqueId() + " to " + id);
        }

        publish(new UserDeletedEventImpl(placeholder));
        publish(new UserCreatedEventImpl(user));

        return user;
    }

    /**
     * Gives up the name of an account whose player has since changed name, so the
     * name's new owner can have it. The account keeps its data under its UUID.
     */
    private void releaseName(UserSnapshot user) {
        if (logger != null) {
            logger.info("Username " + user.name() + " now belongs to another player; renaming the account of " + user.uniqueId() + " to its UUID");
        }

        modifyNow(user.uniqueId(), modifier -> modifier.updateName(user.uniqueId().toString()));
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
    public List<ExpiryRemoval> purgeExpired() {
        synchronized (lock) {
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
                    modifyFromCurrent(user.uniqueId(), current -> modifier -> {
                        modifier.setGroups(current.groups().stream().filter(node -> !node.expired()).collect(Immutables.toList()));

                        current.explicitPermissions().stream().filter(Node::expired).forEach(modifier::removePermission);

                        current.explicitOptions().stream().filter(Node::expired).forEach(modifier::removeOption);
                    });
                }
            }

            return Immutables.copyOfList(removals);
        }
    }

    @Internal
    void renameGroup(String oldName, String newName) {
        synchronized (lock) {
            for (var user : storage.allNow().values()) {
                if (user.groups().stream().anyMatch(parent -> parent.group().equals(oldName))) {

                    modifyFromCurrent(
                            user.uniqueId(),
                            current -> modifier -> modifier.setGroups(
                                    current.groups().stream()
                                            .map(parent -> parent.group().equals(oldName)
                                                    ? ParentNode.builder(parent)
                                                            .group(newName)
                                                            .build()
                                                    : parent)
                                            .collect(Immutables.toList())));
                }
            }
        }
    }

    @Internal
    void removeGroup(String name) {
        synchronized (lock) {
            for (var user : storage.allNow().values()) {
                if (user.groups().stream().anyMatch(parent -> parent.group().equals(name))) {
                    modifyFromCurrent(user.uniqueId(), current -> modifier -> {
                        modifier.setGroups(current.groups().stream()
                                .filter(parent -> !parent.group().equals(name))
                                .collect(Immutables.toList()));
                    });
                }
            }
        }
    }

    private Optional<UserSnapshot> findNow(UUID id) {
        synchronized (lock) {
            var cached = cache.get(id);

            if (cached.isPresent()) {
                return cached.map(UserSnapshot.class::cast);
            }

            return storage.getNow(id)
                    .map(this::expand)
                    .map(user -> {
                        cache(user);

                        return user;
                    });
        }
    }

    private UserSnapshot loadNow(UUID id) {
        synchronized (lock) {
            var user = storage.getNow(id)
                    .map(this::expand)
                    .orElseThrow(() -> new UserNotFoundException(id.toString()));

            cache(user);

            return user;
        }
    }

    private Optional<UserSnapshot> findNameNow(String name) {
        synchronized (lock) {
            var cached = cache.get(name).map(UserSnapshot.class::cast);

            if (cached.isPresent()) {
                return cached;
            }

            return findNameStored(name);
        }
    }

    private Optional<UserSnapshot> findNameStored(String name) {
        synchronized (lock) {
            return storage.getNow(name)
                    .map(this::expand)
                    .map(user -> {
                        cache(user);
                        return user;
                    });
        }
    }

    private User modifyNow(UUID id, Consumer<UserModifier> action) {
        Objects.requireNonNull(action, "action");

        return modifyFromCurrent(id, ignored -> action);
    }

    /**
     * Applies a change worked out from the user as stored now. A change that
     * loses a race with another server is worked out again from a fresh read.
     */
    private User modifyFromCurrent(UUID id, Function<UserSnapshot, Consumer<UserModifier>> change) {
        synchronized (lock) {
            return Mutations.run(store, () -> {
                var previous = loadNow(id);

                var modifier = new UserModifierImpl(previous);

                change.apply(previous).accept(modifier);

                var current = expand(modifier.build(previous));

                validate(current);

                save(current);

                publish(new UserModifiedEventImpl(previous, current));

                return current;
            });
        }
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
        cache(expanded);
    }

    /** Caches a user; undone if the surrounding operation fails. */
    private void cache(UserSnapshot user) {
        Mutations.onRollback(cache.restorer(user.uniqueId()));
        cache.put(user);
    }

    private void uncache(UUID id) {
        Mutations.onRollback(cache.restorer(id));
        cache.remove(id);
    }

    private void publish(Event event) {
        Mutations.afterCommit(() -> events.publish(event));
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
                : new UserSnapshot(user.uniqueId(), user.name(), permissions, user.explicitOptions(), user.groups(), user.revision());
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
