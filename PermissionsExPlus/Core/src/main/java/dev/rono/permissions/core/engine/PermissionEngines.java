package dev.rono.permissions.core.engine;

import dev.rono.permissions.core.engine.casbin.CasbinPermissionEngine;
import dev.rono.permissions.core.resolver.ResolutionSupport;
import java.util.Objects;

public final class PermissionEngines {
    private PermissionEngines() {
        throw new AssertionError();
    }

    public static PermissionEngine create(ResolutionSupport support) {
        Objects.requireNonNull(support, "support");
        return new CasbinPermissionEngine(support);
    }

    public static PermissionEngine createCached(ResolutionSupport support) {
        return new CachedPermissionEngine(create(support));
    }
}
