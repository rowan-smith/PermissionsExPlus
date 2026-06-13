package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.api.ContextPermissionEvaluator;
import ru.tehkode.permissions.PermissionEntity;

import java.util.Map;

final class HolderEntityResolver {

    private final DefaultPermissionManager manager;

    HolderEntityResolver(DefaultPermissionManager manager) {
        this.manager = manager;
    }

    PermissionEntity resolve(PermissionHolder holder) {
        switch (holder.getType()) {
            case USER:
                return manager.getUser(holder.getId());
            case GROUP:
                if (holder instanceof GroupPermissionHolder groupHolder) {
                    return manager.getGroup(groupHolder.groupName());
                }
                throw new IllegalArgumentException("Unsupported group holder: " + holder.getId());
            case WORLD:
            case LADDER:
                throw new UnsupportedOperationException(
                        "Direct permission edits on WORLD/LADDER holders are not supported yet");
            default:
                throw new IllegalArgumentException("Unknown holder type: " + holder.getType());
        }
    }

    String storageRealm(PermissionContext context) {
        return ContextPermissionEvaluator.storageRealm(context, manager.getPlatform());
    }
}
