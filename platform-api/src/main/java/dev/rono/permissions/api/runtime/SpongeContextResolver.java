package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.permission.PermissionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sponge resolver: {@code dimension -> world -> server -> global}.
 */
public final class SpongeContextResolver implements ContextResolver {

    private static final String[] STRIP_ORDER = {
        PermissionContext.DIMENSION, PermissionContext.WORLD, PermissionContext.SERVER
    };

    @Override
    public Optional<String> server(PermissionContext context) {
        return context.get(PermissionContext.SERVER);
    }

    @Override
    public Optional<String> world(PermissionContext context) {
        return context.get(PermissionContext.WORLD);
    }

    @Override
    public Optional<String> dimension(PermissionContext context) {
        return context.get(PermissionContext.DIMENSION);
    }

    @Override
    public List<PermissionContext> inheritanceChain(PermissionContext context) {
        var chain = new ArrayList<PermissionContext>();
        var current = new HashMap<>(context.attributes());
        chain.add(PermissionContext.of(current));

        for (String key : STRIP_ORDER) {
            if (!current.containsKey(key)) {
                continue;
            }
            current = new HashMap<>(current);
            current.remove(key);
            if (!current.isEmpty()) {
                chain.add(PermissionContext.of(current));
            }
        }

        chain.add(PermissionContext.global());
        return List.copyOf(chain);
    }

    @Override
    public Optional<String> storageRealm(PermissionContext context) {
        return dimension(context).or(() -> world(context)).or(() -> server(context));
    }
}
