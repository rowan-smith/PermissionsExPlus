package dev.rono.permissions.core.storage.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class User {

    private final UUID id;
    private final String name;
    private final Instant firstJoin;
    private final Instant lastSeen;
    private final List<UserGroup> groups;
    private final List<UserPermission> permissions;
    private final UserOptions options;

    public User(UUID id,
                String name,
                Instant firstJoin,
                Instant lastSeen,
                List<UserGroup> groups,
                List<UserPermission> permissions,
                UserOptions options) {
        this.id = id;
        this.name = name;
        this.firstJoin = firstJoin;
        this.lastSeen = lastSeen;
        this.groups = List.copyOf(groups);
        this.permissions = List.copyOf(permissions);
        this.options = options;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Instant getFirstJoin() { return firstJoin; }
    public Instant getLastSeen() { return lastSeen; }
    public List<UserGroup> getGroups() { return groups; }
    public List<UserPermission> getPermissions() { return permissions; }
    public UserOptions getOptions() { return options; }
}
