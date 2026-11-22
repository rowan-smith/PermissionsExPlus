package dev.rono.permissions.core.store;

import lombok.var;

import dev.rono.permissions.core.model.GroupSnapshot;
import dev.rono.permissions.core.model.LadderSnapshot;
import dev.rono.permissions.core.model.UserSnapshot;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import dev.rono.permissions.core.util.Immutables;

/**
 * Persistent storage of users, groups and ladders.
 *
 * <p>
 * Every completed write is durable. Saving a snapshot that carries a non-zero
 * revision succeeds only if storage still holds that revision; otherwise it
 * throws {@link StorageConflictException}. Stores shared by several servers
 * check revisions; single-server stores may ignore them.
 * </p>
 */
public interface DataStore extends AutoCloseable {
    void open();

    /**
     * Runs {@code work} so that all of its writes are applied together or not
     * at all. Nested calls join the outer transaction.
     */
    default <T> T transaction(Supplier<T> work) {
        return work.get();
    }

    Optional<String> get(String category, String key);

    Map<String, String> all(String category);

    void put(String category, String key, String payload);

    boolean remove(String category, String key);

    default Optional<UserSnapshot> getUser(UUID id) {
        return get("users", id.toString()).map(SnapshotCodec::user);
    }

    default Map<String, UserSnapshot> allUsers() {
        var users = new LinkedHashMap<String, UserSnapshot>();
        all("users").forEach((key, value) -> users.put(key, SnapshotCodec.user(value)));
        return Immutables.copyOfMap(users);
    }

    default void putUser(UserSnapshot user) {
        put("users", user.uniqueId().toString(), SnapshotCodec.user(user));
    }

    default boolean removeUser(UUID id) {
        return remove("users", id.toString());
    }

    default Optional<GroupSnapshot> getGroup(String name) {
        return get("groups", name).map(SnapshotCodec::group);
    }

    default Map<String, GroupSnapshot> allGroups() {
        var groups = new LinkedHashMap<String, GroupSnapshot>();
        all("groups").forEach((key, value) -> groups.put(key, SnapshotCodec.group(value)));
        return Immutables.copyOfMap(groups);
    }

    default void putGroup(GroupSnapshot group) {
        put("groups", group.name(), SnapshotCodec.group(group));
    }

    default boolean removeGroup(String name) {
        return remove("groups", name);
    }

    default Optional<LadderSnapshot> getLadder(String name) {
        return get("ladders", name).map(SnapshotCodec::ladder);
    }

    default Map<String, LadderSnapshot> allLadders() {
        var ladders = new LinkedHashMap<String, LadderSnapshot>();
        all("ladders").forEach((key, value) -> ladders.put(key, SnapshotCodec.ladder(value)));
        return Immutables.copyOfMap(ladders);
    }

    default void putLadder(LadderSnapshot ladder) {
        put("ladders", ladder.name(), SnapshotCodec.ladder(ladder));
    }

    default boolean removeLadder(String name) {
        return remove("ladders", name);
    }

    default void checkpoint() {}

    default boolean supportsCheckpoints() {
        return false;
    }

    String name();

    boolean persistent();

    @Override
    void close();
}
