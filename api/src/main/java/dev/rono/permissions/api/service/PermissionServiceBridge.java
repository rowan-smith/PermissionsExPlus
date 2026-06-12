package dev.rono.permissions.api.service;

import dev.rono.permissions.api.PermissionsExException;
import dev.rono.permissions.api.backend.BackendHandle;
import dev.rono.permissions.api.backend.BackendInfo;
import dev.rono.permissions.api.data.ImportMode;
import dev.rono.permissions.api.event.PermissionEventBus;
import dev.rono.permissions.api.session.PermissionEditSession;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Runtime operations backing {@link PermissionService}.
 *
 * <p>Implemented by the core manager; not intended for direct use in plugins.</p>
 */
public interface PermissionServiceBridge {

    int userCount();

    int groupCount();

    BackendInfo activeBackend();

    void setActiveBackend(String alias) throws PermissionsExException;

    BackendHandle createBackendHandle(String alias) throws PermissionsExException;

    void importFromBackend(String backendAlias) throws PermissionsExException;

    String exportData() throws PermissionsExException;

    void importData(String document, ImportMode mode) throws PermissionsExException;

    PermissionEventBus events();

    Collection<String> registeredWorlds();

    boolean isDebug();

    List<String> worldInheritance(String world);

    void setWorldInheritance(String world, List<String> parentWorlds);

    Map<String, List<String>> worldInheritanceMap();

    List<Group> defaultGroups(String world);

    Map<Integer, Group> rankLadder(String ladderName);

    Optional<User> lookupUser(String identifier);

    Optional<User> lookupUser(UUID uuid);

    User user(String identifier);

    User user(UUID uuid);

    Set<String> userIdentifiers();

    void deleteUser(String identifier);

    Optional<Group> lookupGroup(String name);

    Group group(String name);

    Set<String> groupNames();

    void deleteGroup(String name);

    void reload() throws PermissionsExException;

    CompletableFuture<Void> reloadAsync();

    PermissionEditSession openEditSession();
}
