package dev.rono.permissions.bukkit17.vault;

import dev.rono.permissions.api.PexApi;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.user.User;
import java.util.Optional;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.plugin.Plugin;
import dev.rono.permissions.core.util.Strings;

public final class PermissionsExPlusVaultChat extends Chat {
    private final Plugin plugin;
    private final PexApi core;

    public PermissionsExPlusVaultChat(Plugin plugin, PermissionsExPlusVaultPermission permission, PexApi core) {
        super(permission);
        this.plugin = plugin;
        this.core = core;
    }

    @Override
    public String getName() {
        return plugin.getDescription().getName();
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getPlayerPrefix(String world, String player) {
        return find(player)
                .map(user -> core.resolvers().options().prefix(user, contexts(world)).orElse(""))
                .orElse("");
    }

    @Override
    public void setPlayerPrefix(String world, String player, String prefix) {
        find(player).ifPresent(user -> core.users().modify(user, modifier -> {
            modifier.setPrefix(prefix, contexts(world));
        }).toCompletableFuture().join());
    }

    @Override
    public String getPlayerSuffix(String world, String player) {
        return find(player)
                .map(user -> core.resolvers().options().suffix(user, contexts(world)).orElse(""))
                .orElse("");
    }

    @Override
    public void setPlayerSuffix(String world, String player, String suffix) {
        find(player).ifPresent(user -> core.users().modify(user, modifier -> {
            modifier.setSuffix(suffix, contexts(world));
        }).toCompletableFuture().join());
    }

    @Override
    public String getGroupPrefix(String world, String group) {
        return core.groups().cache().get(group)
                .map(g -> core.resolvers().options().prefix(g, contexts(world)).orElse(""))
                .orElse("");
    }

    @Override
    public void setGroupPrefix(String world, String group, String prefix) {
        core.groups().cache().get(group).ifPresent(g -> core.groups().modify(g, modifier -> {
            modifier.setPrefix(prefix, contexts(world));
        }).toCompletableFuture().join());
    }

    @Override
    public String getGroupSuffix(String world, String group) {
        return core.groups().cache().get(group)
                .map(g -> core.resolvers().options().suffix(g, contexts(world)).orElse(""))
                .orElse("");
    }

    @Override
    public void setGroupSuffix(String world, String group, String suffix) {
        core.groups().cache().get(group).ifPresent(g -> core.groups().modify(g, modifier -> {
            modifier.setSuffix(suffix, contexts(world));
        }).toCompletableFuture().join());
    }

    @Override
    public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
        return find(player)
                .map(user -> core.resolvers().options().resolve(user, node, contexts(world))
                        .flatMap(this::parseInt)
                        .orElse(defaultValue))
                .orElse(defaultValue);
    }

    @Override
    public void setPlayerInfoInteger(String world, String player, String node, int value) {
        find(player).ifPresent(user -> core.users().modify(user, modifier -> {
            modifier.setOption(node, String.valueOf(value), contexts(world));
        })
                .toCompletableFuture().join());
    }

    @Override
    public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
        return core.groups().cache().get(group)
                .map(g -> core.resolvers().options().resolve(g, node, contexts(world))
                        .flatMap(this::parseInt)
                        .orElse(defaultValue))
                .orElse(defaultValue);
    }

    @Override
    public void setGroupInfoInteger(String world, String group, String node, int value) {
        core.groups().cache().get(group).ifPresent(g -> core.groups().modify(g, modifier -> {
            modifier.setOption(node, String.valueOf(value), contexts(world));
        })
                .toCompletableFuture().join());
    }

    @Override
    public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
        return find(player)
                .map(user -> core.resolvers().options().resolve(user, node, contexts(world))
                        .flatMap(this::parseDouble)
                        .orElse(defaultValue))
                .orElse(defaultValue);
    }

    @Override
    public void setPlayerInfoDouble(String world, String player, String node, double value) {
        find(player).ifPresent(user -> core.users().modify(user, modifier -> {
            modifier.setOption(node, String.valueOf(value), contexts(world));
        })
                .toCompletableFuture().join());
    }

    @Override
    public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
        return core.groups().cache().get(group)
                .map(g -> core.resolvers().options().resolve(g, node, contexts(world))
                        .flatMap(this::parseDouble)
                        .orElse(defaultValue))
                .orElse(defaultValue);
    }

    @Override
    public void setGroupInfoDouble(String world, String group, String node, double value) {
        core.groups().cache().get(group).ifPresent(g -> core.groups().modify(g, modifier -> {
            modifier.setOption(node, String.valueOf(value), contexts(world));
        })
                .toCompletableFuture().join());
    }

    @Override
    public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
        return find(player)
                .map(user -> core.resolvers().options().resolve(user, node, contexts(world))
                        .flatMap(this::parseBoolean)
                        .orElse(defaultValue))
                .orElse(defaultValue);
    }

    @Override
    public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {
        find(player).ifPresent(user -> core.users().modify(user, modifier -> {
            modifier.setOption(node, String.valueOf(value), contexts(world));
        })
                .toCompletableFuture().join());
    }

    @Override
    public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
        return core.groups().cache().get(group)
                .map(g -> core.resolvers().options().resolve(g, node, contexts(world))
                        .flatMap(this::parseBoolean)
                        .orElse(defaultValue))
                .orElse(defaultValue);
    }

    @Override
    public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
        core.groups().cache().get(group).ifPresent(g -> core.groups().modify(g, modifier -> {
            modifier.setOption(node, String.valueOf(value), contexts(world));
        })
                .toCompletableFuture().join());
    }

    @Override
    public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
        return find(player)
                .map(user -> core.resolvers().options().resolve(user, node, contexts(world)).orElse(defaultValue))
                .orElse(defaultValue);
    }

    @Override
    public void setPlayerInfoString(String world, String player, String node, String value) {
        find(player).ifPresent(user -> core.users().modify(user, modifier -> {
            modifier.setOption(node, value, contexts(world));
        })
                .toCompletableFuture().join());
    }

    @Override
    public String getGroupInfoString(String world, String group, String node, String defaultValue) {
        return core.groups().cache().get(group)
                .map(g -> core.resolvers().options().resolve(g, node, contexts(world)).orElse(defaultValue))
                .orElse(defaultValue);
    }

    @Override
    public void setGroupInfoString(String world, String group, String node, String value) {
        core.groups().cache().get(group).ifPresent(g -> core.groups().modify(g, modifier -> {
            modifier.setOption(node, value, contexts(world));
        })
                .toCompletableFuture().join());
    }

    private ContextSet contexts(String world) {
        return world == null || Strings.isBlank(world) ? ContextSet.empty() : ContextSet.builder().add("world", world).build();
    }

    private Optional<User> find(String name) {
        try {
            return core.users().find(name).toCompletableFuture().join();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Optional<Double> parseDouble(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Optional<Boolean> parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Optional.of(Boolean.parseBoolean(value));
        }

        return Optional.empty();
    }
}
