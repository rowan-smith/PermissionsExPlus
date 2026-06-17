package dev.rono.permissions.proxy.commands;

import dev.rono.permissions.bungee.BungeePermissionsExConfig;
import dev.rono.permissions.core.commands.CoreCommandService;

/**
 * Shared {@link CoreCommandService.ConfigBridge} for proxy platforms using {@link BungeePermissionsExConfig}.
 */
public final class ProxyConfigBridge implements CoreCommandService.ConfigBridge {
    private final BungeePermissionsExConfig config;

    public ProxyConfigBridge(BungeePermissionsExConfig config) {
        this.config = config;
    }

    @Override
    public Object get(String path) {
        return config.getNode(path);
    }

    @Override
    public void set(String path, Object value) {
        config.setNode(path, value);
    }

    @Override
    public void save() {
        config.save();
    }
}
