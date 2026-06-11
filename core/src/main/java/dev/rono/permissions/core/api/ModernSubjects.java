package dev.rono.permissions.core.api;

import dev.rono.permissions.api.RankingException;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.User;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

final class ModernSubjects {
    private ModernSubjects() {}

    static PermissionUser requireUser(User user) {
        if (user instanceof ModernUserAdapter adapter) {
            return adapter.delegate();
        }
        throw new IllegalArgumentException("Unsupported User implementation: " + user.getClass().getName());
    }

    static PermissionUser optionalUser(User user) {
        return user == null ? null : requireUser(user);
    }

    static RankingException toRankingException(ru.tehkode.permissions.exceptions.RankingException legacy) {
        return new RankingException(legacy.getMessage(), legacy);
    }

    static Group wrapGroup(PermissionGroup group, dev.rono.permissions.core.DefaultPermissionManager manager) {
        return new ModernGroupAdapter(group, manager);
    }
}
