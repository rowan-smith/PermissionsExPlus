package ru.tehkode.permissions;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.bukkit.PermissionsExConfig;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
 * Classic PermissionsEx permission manager contract ({@code ru.tehkode.permissions}, baseline {@code 628215f}).
 *
 * <p>Runtime extensions ({@code PlatformAdapter}, entity bus dispatches, internal scheduling) are implemented on
 * {@code dev.rono.permissions.core.InternalPermissionManager} and are not part of this surface.</p>
 */
public interface PermissionManager {

    int TRANSIENT_PERMISSION = 0;

    boolean shouldCreateUserRecords();

    PermissionsExConfig getConfiguration();

    boolean has(Player player, String permission);

    boolean has(Player player, String permission, String world);

    boolean has(String playerName, String permission, String world);

    boolean has(UUID playerId, String permission, String world);

    PermissionUser getUser(String username);

    void cacheUser(String ident, String fallbackName);

    PermissionUser getUser(Player player);

    PermissionUser getUser(UUID uid);

    Set<PermissionUser> getUsers();

    Set<PermissionUser> getActiveUsers();

    Collection<String> getUserIdentifiers();

    Collection<String> getUserNames();

    Set<PermissionUser> getUsers(String groupName, String worldName);

    Set<PermissionUser> getUsers(String groupName);

    Set<PermissionUser> getUsers(String groupName, String worldName, boolean inheritance);

    Set<PermissionUser> getUsers(String groupName, boolean inheritance);

    void resetUser(String userName);

    void resetUser(Player player);

    void clearUserCache(String userName);

    void clearUserCache(UUID uid);

    void clearUserCache(Player player);

    PermissionGroup getGroup(String groupname);

    List<PermissionGroup> getGroupList();

    @Deprecated
    PermissionGroup[] getGroups();

    @Deprecated
    Collection<String> getGroupNames();

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

    void setBackend(String backendName) throws PermissionBackendException;

    PermissionBackend createBackend(String backendName) throws PermissionBackendException;

    void reset() throws PermissionBackendException;

    void reset(boolean callEvent) throws PermissionBackendException;

    void end();

    void initTimer();

    PermissionMatcher getPermissionMatcher();

    void setPermissionMatcher(PermissionMatcher matcher);

    Logger getLogger();

    ScheduledExecutorService getExecutor();

    boolean shouldSaveDefaultGroup();
}
