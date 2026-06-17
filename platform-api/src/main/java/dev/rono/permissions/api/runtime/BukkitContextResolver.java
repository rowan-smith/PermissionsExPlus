package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.permission.PermissionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Game-server resolver: {@code world -> server -> global}.
 */
public class BukkitContextResolver implements ContextResolver {

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
        return Optional.empty();
    }

    @Override
    public List<PermissionContext> inheritanceChain(PermissionContext context) {
        var chain = new ArrayList<PermissionContext>();
        chain.add(context);
        if (world(context).isPresent()) {
            server(context).ifPresent(server -> chain.add(PermissionContext.server(server)));
        }
        chain.add(PermissionContext.global());
        return List.copyOf(chain);
    }

    @Override
    public Optional<String> storageRealm(PermissionContext context) {
        return world(context).or(() -> server(context));
    }
}
