package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.permission.PermissionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Proxy resolver: {@code server -> global}. No worlds on proxy hosts.
 */
public class ProxyContextResolver implements ContextResolver {

    @Override
    public Optional<String> server(PermissionContext context) {
        return context.get(PermissionContext.SERVER);
    }

    @Override
    public Optional<String> world(PermissionContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<String> dimension(PermissionContext context) {
        return Optional.empty();
    }

    @Override
    public List<PermissionContext> inheritanceChain(PermissionContext context) {
        var chain = new ArrayList<PermissionContext>();
        chain.add(context);
        chain.add(PermissionContext.global());
        return List.copyOf(chain);
    }

    @Override
    public Optional<String> storageRealm(PermissionContext context) {
        return server(context).or(() -> context.get(PermissionContext.WORLD));
    }
}
