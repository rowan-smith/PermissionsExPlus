package dev.rono.permissions.core.manager;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.exception.GroupNotFoundException;
import dev.rono.permissions.api.exception.InvalidLadderException;
import dev.rono.permissions.api.exception.LadderAlreadyExistsException;
import dev.rono.permissions.api.exception.LadderNotFoundException;
import dev.rono.permissions.api.exception.UserNotFoundException;
import dev.rono.permissions.api.ladder.Ladder;
import dev.rono.permissions.api.ladder.LadderManager;
import dev.rono.permissions.api.ladder.LadderModifier;
import dev.rono.permissions.api.ladder.PromotionResult;
import dev.rono.permissions.api.ladder.PromotionStatus;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.core.event.EventBusImpl;
import dev.rono.permissions.core.event.LadderCreatedEventImpl;
import dev.rono.permissions.core.event.LadderDeletedEventImpl;
import dev.rono.permissions.core.event.LadderModifiedEventImpl;
import dev.rono.permissions.core.event.UserDemotedEventImpl;
import dev.rono.permissions.core.event.UserPromotedEventImpl;
import dev.rono.permissions.core.model.LadderSnapshot;
import dev.rono.permissions.core.model.PromotionResultImpl;
import dev.rono.permissions.core.modifier.LadderModifierImpl;
import dev.rono.permissions.core.store.DataStore;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import dev.rono.permissions.core.util.Immutables;

public final class LadderManagerImpl implements LadderManager {
    private final LadderCacheManagerImpl cache;
    private final LadderStorageManagerImpl storage;

    private final EventBusImpl events;
    private final Executor executor;

    private UserManagerImpl users;
    private GroupManagerImpl groups;

    public LadderManagerImpl(DataStore store, EventBusImpl events) {
        this(store, events, Runnable::run);
    }

    public LadderManagerImpl(DataStore store, EventBusImpl events, Executor executor) {
        this.events = Objects.requireNonNull(events, "events");
        this.executor = Objects.requireNonNull(executor, "executor");

        this.cache = new LadderCacheManagerImpl();
        this.storage = new LadderStorageManagerImpl(store, executor);
    }

    public void attach(UserManagerImpl users, GroupManagerImpl groups) {
        this.users = users;
        this.groups = groups;
    }

    @Override
    public LadderCacheManagerImpl cache() {
        return cache;
    }

    @Override
    public LadderStorageManagerImpl storage() {
        return storage;
    }

    @Override
    public CompletionStage<Ladder> modify(String name, Consumer<LadderModifier> action) {
        return Stages.call(() -> modifyNow(name, action), executor);
    }

    @Override
    public CompletionStage<Optional<Ladder>> find(String name) {
        return Stages.call(() -> findNow(name).map(Ladder.class::cast), executor);
    }

    @Override
    public CompletionStage<PromotionResult> promote(String username, String ladder) {
        return users.find(username).thenCompose(user -> {
            if (!user.isPresent()) {
                return Stages.failed(new UserNotFoundException(username));
            }

            return promote(user.get().uniqueId(), ladder);
        });
    }

    @Override
    public CompletionStage<PromotionResult> promote(UUID id, String ladder, ContextSet contexts) {
        return move(id, ladder, contexts, true);
    }

    @Override
    public CompletionStage<PromotionResult> demote(String username, String ladder) {
        return users.find(username).thenCompose(user -> {
            if (!user.isPresent()) {
                return Stages.failed(new UserNotFoundException(username));
            }

            return demote(user.get().uniqueId(), ladder);
        });
    }

    @Override
    public CompletionStage<PromotionResult> demote(UUID id, String ladder, ContextSet contexts) {
        return move(id, ladder, contexts, false);
    }

    public CompletionStage<Ladder> create(String name) {
        Objects.requireNonNull(name, "name");

        return Stages.call(() -> {
            synchronized (this) {
                var key = Identifiers.ladder(name);

                if (storage.getNow(key).isPresent()) {
                    throw new LadderAlreadyExistsException(key);
                }

                var ladder = new LadderSnapshot(key, Immutables.listOf());

                save(ladder);
                events.publish(new LadderCreatedEventImpl(ladder));

                return ladder;
            }
        }, executor);
    }

    public CompletionStage<Boolean> delete(String name) {
        return Stages.call(() -> {
            synchronized (this) {
                var key = Identifiers.ladder(name);

                var previous = findNow(key);

                var changed = storage.deleteNow(key);

                if (!changed) {
                    return false;
                }

                cache.remove(key);

                previous.ifPresent(ladder -> {
                    events.publish(new LadderDeletedEventImpl(ladder));
                });

                return true;
            }
        }, executor);
    }

    public CompletionStage<Ladder> load(String name) {
        return Stages.call(() -> loadNow(name), executor);
    }

    public synchronized void loadAll() {
        cache.clear();

        storage.allNow()
                .values()
                .forEach(cache::put);
    }

    synchronized void renameGroup(String oldName, String newName) {
        for (var ladder : storage.allNow().values()) {
            if (ladder.groups().contains(oldName)) {
                modifyNow(ladder.name(), modifier -> modifier.setGroups(ladder.groups().stream().map(group -> group.equals(oldName) ? newName : group).collect(Immutables.toList())));
            }
        }
    }

    synchronized void removeGroup(String name) {
        for (var ladder : storage.allNow().values()) {
            if (ladder.groups().contains(name)) {
                modifyNow(ladder.name(), modifier -> modifier.remove(name));
            }
        }
    }

    private synchronized Optional<LadderSnapshot> findNow(String name) {
        var key = Identifiers.ladder(name);
        var cached = cache.get(key).map(LadderSnapshot.class::cast);

        if (cached.isPresent()) {
            return cached;
        }

        return storage.getNow(key).map(ladder -> {
            cache.put(ladder);

            return ladder;
        });
    }

    private synchronized LadderSnapshot loadNow(String name) {
        var key = Identifiers.ladder(name);

        var ladder = storage.getNow(key).orElseThrow(() -> new LadderNotFoundException(key));

        cache.put(ladder);

        return ladder;
    }

    private synchronized Ladder modifyNow(String name, Consumer<LadderModifier> action) {
        Objects.requireNonNull(action, "action");

        var previous = loadNow(name);

        var modifier = new LadderModifierImpl(previous);

        action.accept(modifier);

        var current = modifier.build(previous);

        validate(current);

        save(current);

        events.publish(new LadderModifiedEventImpl(previous, current));

        return current;
    }

    private void validate(Ladder ladder) {
        if (groups == null) {
            throw new IllegalStateException("Group manager has not been attached");
        }

        var seen = new HashSet<String>();

        for (var groupName : ladder.groups()) {
            var key = Identifiers.group(groupName);

            if (!seen.add(key)) {
                throw new InvalidLadderException("Duplicate ladder group: " + key);
            }

            if (!groups.findNow(key).isPresent()) {
                throw new GroupNotFoundException(key);
            }
        }
    }

    private void save(LadderSnapshot ladder) {
        storage.saveNow(ladder);
        cache.put(ladder);
    }

    private CompletionStage<PromotionResult> move(UUID id, String ladderName, ContextSet contexts, boolean promote) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(contexts, "contexts");

        final LadderSnapshot ladder;

        synchronized (this) {
            ladder = loadNow(ladderName);
        }

        return users.load(id).thenCompose(user -> {
            var memberships = user.groups().stream()
                    .filter(node -> {
                        return !node.expired() && node.contexts().equals(contexts) && ladder.contains(node.group());
                    })
                    .collect(Immutables.toList());

            if (memberships.size() > 1) {
                return completedResult(user, ladder, Optional.empty(), Optional.empty(), PromotionStatus.MULTIPLE_POSITIONS, promote);
            }

            if (memberships.isEmpty()) {
                return completedResult(user, ladder, Optional.empty(), Optional.empty(), PromotionStatus.NOT_ON_LADDER, promote);
            }

            var previous = memberships.get(0);
            int position = ladder.positionOf(previous.group()).getAsInt();

            if (promote && position == ladder.size() - 1) {
                return completedResult(user, ladder, Optional.of(previous), Optional.of(previous), PromotionStatus.ALREADY_TOP, true);
            }

            if (!promote && position == 0) {
                return completedResult(user, ladder, Optional.of(previous), Optional.of(previous), PromotionStatus.ALREADY_BOTTOM, false);
            }

            var target = ladder.groups().get(position + (promote ? 1 : -1));

            var current = ParentNode.builder(previous)
                    .group(target)
                    .build();

            return users.modify(id, modifier -> {
                modifier.removeGroup(previous);
                modifier.addGroup(current);
            }).thenApply(updated -> result(updated, ladder, Optional.of(previous), Optional.of(current), promote ? PromotionStatus.PROMOTED : PromotionStatus.DEMOTED, promote));
        });
    }

    private CompletionStage<PromotionResult> completedResult(User user, Ladder ladder, Optional<ParentNode> previous, Optional<ParentNode> current, PromotionStatus status, boolean promote) {
        return CompletableFuture.completedFuture(result(user, ladder, previous, current, status, promote));
    }

    private PromotionResult result(User user, Ladder ladder, Optional<ParentNode> previous, Optional<ParentNode> current, PromotionStatus status, boolean promote) {
        var result = new PromotionResultImpl(user, ladder, previous, current, status);

        if (status == PromotionStatus.PROMOTED) {
            events.publish(new UserPromotedEventImpl(result));
        }

        if (status == PromotionStatus.DEMOTED) {
            events.publish(new UserDemotedEventImpl(result));
        }

        return result;
    }
}
