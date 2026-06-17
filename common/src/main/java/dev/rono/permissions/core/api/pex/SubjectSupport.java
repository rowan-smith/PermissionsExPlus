package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.RankingException;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.DefaultPermissionManager;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class SubjectSupport {

    private SubjectSupport() {}

    static User wrapUser(DefaultPermissionManager manager, PermissionUser user) {
        return new UserImpl(parseUserId(user), user, manager);
    }

    static PermissionUser requireUser(User user) {
        if (user instanceof UserImpl impl) {
            return impl.delegate();
        }
        throw new IllegalArgumentException("Unsupported User implementation: " + user.getClass().getName());
    }

    static PermissionUser optionalUser(User user) {
        return user == null ? null : requireUser(user);
    }

    static RankingException toRankingException(ru.tehkode.permissions.exceptions.RankingException legacy) {
        return new RankingException(legacy.getMessage(), legacy);
    }

    static Group wrapGroup(PermissionGroup group, DefaultPermissionManager manager) {
        return new GroupImpl(group.getIdentifier(), group, manager);
    }

    private static UUID parseUserId(PermissionUser user) {
        try {
            return UUID.fromString(user.getIdentifier());
        } catch (IllegalArgumentException ex) {
            return UUID.nameUUIDFromBytes(user.getIdentifier().getBytes(StandardCharsets.UTF_8));
        }
    }
}
