package dev.rono.permissions.core.api;

import dev.rono.permissions.api.session.PermissionEditSession;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.core.DefaultPermissionManager;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class PermissionEditSessionImpl implements PermissionEditSession {
    private final DefaultPermissionManager manager;
    private final Set<String> userIds = new LinkedHashSet<>();
    private final Set<String> groupNames = new LinkedHashSet<>();
    private boolean closed;

    public PermissionEditSessionImpl(DefaultPermissionManager manager) {
        this.manager = manager;
    }

    @Override
    public User user(String identifier) {
        ensureOpen();
        userIds.add(identifier);
        return manager.user(identifier);
    }

    @Override
    public User user(UUID uuid) {
        ensureOpen();
        userIds.add(uuid.toString());
        return manager.user(uuid);
    }

    @Override
    public Group group(String name) {
        ensureOpen();
        groupNames.add(name);
        return manager.group(name);
    }

    @Override
    public void save() {
        ensureOpen();
        for (String userId : userIds) {
            manager.getUser(userId).save();
        }
        for (String groupName : groupNames) {
            manager.getGroup(groupName).save();
        }
    }

    @Override
    public void close() {
        closed = true;
        userIds.clear();
        groupNames.clear();
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("PermissionEditSession is closed");
        }
    }
}
