package dev.rono.permissions.api.service;

import dev.rono.permissions.api.PermissionsExException;
import dev.rono.permissions.api.backend.BackendHandle;
import dev.rono.permissions.api.backend.BackendInfo;
import dev.rono.permissions.api.data.ImportMode;
import dev.rono.permissions.api.event.PermissionEventBus;
import dev.rono.permissions.api.query.PermissionQuery;
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
 * Modern PermissionsEx integration API ({@code dev.rono.permissions.api}).
 *
 * <p>Primary entry: {@link #query()} — e.g. {@code service.query().world(w).user(uuid).inGroup("vip")},
 * {@code service.query().groups().count()}, {@code service.query().backend().activate("file")}.</p>
 */
public interface PermissionService {

    /** Single fluent entry for checks, edits, registry, and backend operations. */
    default PermissionQuery query() {
        return PermissionQuery.of(this);
    }

    // --- Registry counts (deprecated — use query().users().count() / query().groups().count()) ---

    /** @deprecated use {@link PermissionQuery#users()} {@code .count()} */
    @Deprecated
    default int registeredUserNameCount() {
        return userCount();
    }

    /** @deprecated use {@link PermissionQuery#groups()} {@code .count()} */
    @Deprecated
    default int registeredGroupCount() {
        return groupCount();
    }

    /** @deprecated use {@link PermissionQuery#backend()} {@code .simpleName()} */
    @Deprecated
    default String activeBackendSimpleName() {
        return backend().simpleName();
    }

    /** @deprecated use {@link PermissionQuery#users()} {@code .count()} */
    @Deprecated
    int userCount();

    /** @deprecated use {@link PermissionQuery#groups()} {@code .count()} */
    @Deprecated
    int groupCount();

    // --- Backend (deprecated — use query().backend()) ---

    /** @deprecated use {@link PermissionQuery#backend()} {@code .info()} */
    @Deprecated
    BackendInfo backend();

    /** @deprecated use {@link PermissionQuery#backend()} {@code .activate(alias)} */
    @Deprecated
    void setActiveBackend(String alias) throws PermissionsExException;

    /** @deprecated use {@link PermissionQuery#backend()} {@code .createHandle(alias)} */
    @Deprecated
    BackendHandle createBackendHandle(String alias) throws PermissionsExException;

    /** @deprecated use {@link PermissionQuery#backend()} {@code .importFrom(alias)} */
    @Deprecated
    void importFromBackend(String backendAlias) throws PermissionsExException;

    /** @deprecated use {@link PermissionQuery#backend()} {@code .exportData()} */
    @Deprecated
    String exportData() throws PermissionsExException;

    /** @deprecated use {@link PermissionQuery#backend()} {@code .importData(document, mode)} */
    @Deprecated
    void importData(String document, ImportMode mode) throws PermissionsExException;

    // --- Server state (also available on {@link PermissionQuery}) ---

    PermissionEventBus events();

    Collection<String> worlds();

    boolean isDebug();

    // --- World inheritance (used by {@link dev.rono.permissions.api.query.WorldScope}) ---

    List<String> worldInheritance(String world);

    void setWorldInheritance(String world, List<String> parentWorlds);

    Map<String, List<String>> worldInheritanceMap();

    List<Group> defaultGroups(String world);

    Map<Integer, Group> rankLadder(String ladderName);

    // --- Subjects (direct access; prefer query().world(w).user(...) in plugins) ---

    Optional<User> findUser(String identifier);

    Optional<User> findUser(UUID uuid);

    User user(String identifier);

    User user(UUID uuid);

    Set<String> userIdentifiers();

    void deleteUser(String identifier);

    Optional<Group> findGroup(String name);

    Group group(String name);

    Set<String> groupNames();

    void deleteGroup(String name);

    // --- Maintenance (also on {@link PermissionQuery}) ---

    void reload() throws PermissionsExException;

    CompletableFuture<Void> reloadAsync();

    PermissionEditSession openEditSession();
}
