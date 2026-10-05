package dev.rono.permissions.core.manager;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.group.GroupStorageManager;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.core.model.GroupSnapshot;
import dev.rono.permissions.core.store.DataStore;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public final class GroupStorageManagerImpl implements GroupStorageManager {

    private final DataStore store;
    private final Executor executor;

    public GroupStorageManagerImpl(DataStore store, Executor executor) {
        this.store = Objects.requireNonNull(store, "store");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public CompletionStage<Optional<Group>> get(String name) {
        return Stages.call(() -> getNow(name).map(Group.class::cast), executor);
    }

    public CompletionStage<Set<String>> identifiers() {
        return Stages.call(() -> Set.copyOf(store.allGroups().keySet()), executor);
    }

    Optional<GroupSnapshot> getNow(String name) {
        return store.getGroup(Identifiers.group(name));
    }

    Map<String, GroupSnapshot> allNow() {
        return store.allGroups();
    }

    void saveNow(GroupSnapshot group) {
        store.putGroup(group);
    }

    boolean deleteNow(String name) {
        return store.removeGroup(Identifiers.group(name));
    }
}
