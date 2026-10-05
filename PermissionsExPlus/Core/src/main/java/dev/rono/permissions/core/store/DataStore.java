package dev.rono.permissions.core.store;

import dev.rono.permissions.core.model.GroupSnapshot;
import dev.rono.permissions.core.model.LadderSnapshot;
import dev.rono.permissions.core.model.UserSnapshot;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DataStore extends AutoCloseable {
    void open();

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
        return Map.copyOf(users);
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
        return Map.copyOf(groups);
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
        return Map.copyOf(ladders);
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
