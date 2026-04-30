package ru.tehkode.permissions;

import dev.rono.permissions.api.bus.EntityMutation;
import dev.rono.permissions.api.runtime.PlatformAdapter;
import java.util.Collection;
import java.util.TimerTask;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
 * Classic PermissionsEx permission manager contract ({@code ru.tehkode.permissions}).
 */
public interface PermissionManager {

    int TRANSIENT_PERMISSION = 0;

    boolean shouldCreateUserRecords();

    /** Plugin data directory path used when backends write backups or sidecar files (classic {@code config.yml} {@code basedir}). */
    String getBasedir();

    /** Persists root plugin configuration when a backend requests it (for example SQL alias refresh). */
    default void saveMainConfiguration() {}

    PlatformAdapter getPlatform();

    boolean allowOps();

    boolean userAddGroupsLast();

    void registerTask(TimerTask task, int delay);

    void publishEntity(String entityIdentifier, String entityType, EntityMutation mutation);

    void scheduleTimedGroupsCheck(long nextExpiration, String identifier);

    boolean has(String playerName, String permission, String world);

    boolean has(UUID playerId, String permission, String world);

    PermissionUser getUser(String username);

    void cacheUser(String ident, String fallbackName);

    PermissionUser getUser(UUID uid);

    Set<PermissionUser> getUsers();

    Set<PermissionUser> getActiveUsers();

    Set<PermissionUser> getActiveUsers(String groupName);

    Set<PermissionUser> getActiveUsers(String groupName, boolean inheritance);

    Collection<String> getUserIdentifiers();

    Collection<String> getUserNames();

    Set<PermissionUser> getUsers(String groupName, String worldName);

    Set<PermissionUser> getUsers(String groupName);

    Set<PermissionUser> getUsers(String groupName, String worldName, boolean inheritance);

    Set<PermissionUser> getUsers(String groupName, boolean inheritance);

    void resetUser(String userName);

    void clearUserCache(String userName);

    void clearUserCache(UUID uid);

    PermissionGroup getDefaultGroup();

    PermissionGroup getDefaultGroup(String worldName);

    PermissionGroup getGroup(String groupname);

    List<PermissionGroup> getGroupList();

    @Deprecated
    PermissionGroup[] getGroups();

    @Deprecated
    String[] getGroupNames();

    List<PermissionGroup> getGroups(String groupName, String worldName);

    List<PermissionGroup> getGroups(String groupName);

    List<PermissionGroup> getGroups(String groupName, String worldName, boolean inheritance);

    List<PermissionGroup> getGroups(String groupName, boolean inheritance);

    List<PermissionGroup> getDefaultGroups(String worldName);

    PermissionGroup resetGroup(String groupName);

    void setDebug(boolean debug);

    boolean isDebug();

    Map<Integer, PermissionGroup> getRankLadder(String ladderName);

    List<String> getWorldInheritance(String worldName);

    void setWorldInheritance(String world, List<String> parentWorlds);

    PermissionBackend getBackend();

    int registeredUserNameCount();

    int registeredGroupCount();

    String activeBackendSimpleName();

    void setBackend(String backendName) throws PermissionBackendException;

    PermissionBackend createBackend(String backendName) throws PermissionBackendException;

    void reset() throws PermissionBackendException;

    void reset(boolean callEvent) throws PermissionBackendException;

    void end();

    void initTimer();

    PermissionMatcher getPermissionMatcher();

    void setPermissionMatcher(PermissionMatcher matcher);

    Collection<String> getWorldNames();

    Logger getLogger();

    ScheduledExecutorService getExecutor();

    boolean shouldSaveDefaultGroup();

    /** Classic alias for {@link #reset()}. */
    default void reload() throws PermissionBackendException {
        reset();
    }

    /** Compatibility alias aligning with APIs that historically used {@code createUser}; delegates to {@link #getUser(String)}. */
    default PermissionUser createUser(String identifier) {
        return getUser(identifier);
    }

    /** @see #createUser(String) */
    default PermissionUser createUser(UUID uuid) {
        return getUser(uuid);
    }

    default void removeUser(String identifier) {
        try {
            getUser(identifier).remove();
        } catch (Throwable first) {
            if (getBackend().hasUser(identifier)) {
                getBackend().getUserData(identifier).remove();
            }
        }
        resetUser(identifier.toLowerCase());
        try {
            clearUserCache(UUID.fromString(identifier));
        } catch (IllegalArgumentException notUuidLiteral) {
            try {
                clearUserCache(identifier);
            } catch (Throwable ignored) {
            }
        }
    }

    default void removeGroup(String groupName) {
        if (groupName == null || groupName.isEmpty()) {
            return;
        }
        try {
            PermissionGroup group = getGroup(groupName);
            group.remove();
        } catch (Throwable first) {
            if (getBackend().hasGroup(groupName)) {
                getBackend().getGroupData(groupName).remove();
            }
        }
        resetGroup(groupName);
    }

    default void removeUser(UUID uuid) {
        removeUser(uuid.toString());
    }
}
