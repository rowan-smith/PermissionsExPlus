package dev.rono.permissions.core;

import dev.rono.permissions.api.bus.EntityMutation;
import dev.rono.permissions.api.runtime.PlatformAdapter;
import java.util.Collection;
import java.util.Set;
import java.util.TimerTask;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

/**
 * Core-runtime hooks not part of the classic {@link PermissionManager} compile surface ({@code 628215f}).
 */
public interface InternalPermissionManager extends PermissionManager {

    PlatformAdapter getPlatform();

    void publishEntity(String entityIdentifier, String entityType, EntityMutation mutation);

    String getBasedir();

    void saveMainConfiguration();

    boolean allowOps();

    boolean userAddGroupsLast();

    void registerTask(TimerTask task, int delay);

    void scheduleTimedGroupsCheck(long nextExpiration, String identifier);

    Collection<String> getWorldNames();

    PermissionGroup getDefaultGroup();

    PermissionGroup getDefaultGroup(String worldName);

    Set<PermissionUser> getActiveUsers(String groupName);

    Set<PermissionUser> getActiveUsers(String groupName, boolean inheritance);

    static InternalPermissionManager require(PermissionManager manager) {
        if (manager instanceof InternalPermissionManager internal) {
            return internal;
        }
        throw new IllegalStateException("PermissionManager is not an InternalPermissionManager: " + manager.getClass().getName());
    }
}
