package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.PermissionsExApi;
import dev.rono.permissions.api.event.PermissionEventBus;
import dev.rono.permissions.api.group.GroupManager;
import dev.rono.permissions.api.ladder.LadderManager;
import dev.rono.permissions.api.realm.RealmManager;
import dev.rono.permissions.api.user.UserManager;
import dev.rono.permissions.core.DefaultPermissionManager;
import ru.tehkode.permissions.PermissionManager;

public final class PermissionsExApiImpl implements PermissionsExApi {

    private final DefaultPermissionManager manager;
    private final UserManager userManager;
    private final GroupManager groupManager;
    private final DefaultRealmManager realmManager;
    private final LadderManager ladderManager;

    public PermissionsExApiImpl(DefaultPermissionManager manager) {
        this.manager = manager;
        this.userManager = new DefaultUserManager(manager);
        this.groupManager = new DefaultGroupManager(manager);
        this.realmManager = new DefaultRealmManager(manager);
        this.ladderManager = new DefaultLadderManager(manager);
    }

    @Override
    public UserManager getUserManager() {
        return userManager;
    }

    @Override
    public GroupManager getGroupManager() {
        return groupManager;
    }

    @Override
    public RealmManager getRealmManager() {
        return realmManager;
    }

    @Override
    public LadderManager getLadderManager() {
        return ladderManager;
    }

    @Override
    public PermissionManager getPermissionManager() {
        return manager;
    }

    @Override
    public PermissionEventBus getEventBus() {
        return manager.events();
    }
}
