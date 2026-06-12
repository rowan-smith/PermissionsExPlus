package dev.rono.permissions.core.api;

import dev.rono.permissions.api.PexRankingException;
import dev.rono.permissions.api.subject.PexGroup;
import dev.rono.permissions.api.subject.PexUser;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

final class ModernSubjects {
    private ModernSubjects() {}

    static PermissionUser requireUser(PexUser user) {
        if (user instanceof ModernUserAdapter adapter) {
            return adapter.delegate();
        }
        throw new IllegalArgumentException("Unsupported PexUser implementation: " + user.getClass().getName());
    }

    static PermissionUser optionalUser(PexUser user) {
        return user == null ? null : requireUser(user);
    }

    static PexRankingException toRankingException(ru.tehkode.permissions.exceptions.RankingException legacy) {
        return new PexRankingException(legacy.getMessage(), legacy);
    }

    static PexGroup wrapGroup(PermissionGroup group, dev.rono.permissions.core.DefaultPermissionManager manager) {
        return new ModernGroupAdapter(group, manager);
    }
}
