package dev.rono.permissions.spigot.vault;

import dev.rono.permissions.api.PexApi;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.user.User;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.function.Function;

public final class PermissionsExPlusVaultPermission extends Permission {
    private final Plugin plugin;
    private final PexApi core;

    public PermissionsExPlusVaultPermission(Plugin plugin, PexApi core) {
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
    public boolean hasSuperPermsCompat() {
        return true;
    }

    @Override
    public boolean playerHas(String world, String player, String permission) {
        return user(player, user -> core.resolvers().permissions().hasPermission(user, permission, contexts(world)));
    }

    @Override
    public boolean playerAdd(String world, String player, String permission) {
        return user(player, user -> {
            core.users().modify(user, modifier -> modifier.allowPermission(permission, contexts(world))).toCompletableFuture().join();
            return true;
        });
    }

    @Override
    public boolean playerRemove(String world, String player, String permission) {
        return user(player, user -> {
            core.users().modify(user, modifier -> modifier.removePermission(permission, contexts(world))).toCompletableFuture().join();
            return true;
        });
    }

    @Override
    public boolean groupHas(String world, String group, String permission) {
        return core.groups().cache().get(group)
                .map(value -> core.resolvers().permissions().hasPermission(value, permission, contexts(world)))
                .orElse(false);
    }

    @Override
    public boolean groupAdd(String world, String group, String permission) {
        return core.groups().cache().get(group).map(value -> {
            core.groups().modify(value, modifier -> modifier.allowPermission(permission, contexts(world))).toCompletableFuture().join();
            return true;
        }).orElse(false);
    }

    @Override
    public boolean groupRemove(String world, String group, String permission) {
        return core.groups().cache().get(group).map(value -> {
            core.groups().modify(value, modifier -> modifier.removePermission(permission, contexts(world))).toCompletableFuture().join();
            return true;
        }).orElse(false);
    }

    @Override
    public boolean playerInGroup(String world, String player, String group) {
        return user(player, user -> core.resolvers().inheritance().inherits(user, group, contexts(world)));
    }

    @Override
    public boolean playerAddGroup(String world, String player, String group) {
        return core.groups().cache().get(group).map(value -> user(player, user -> {
            core.users().modify(user, modifier -> modifier.addGroup(group, contexts(world))).toCompletableFuture().join();
            return true;
        })).orElse(false);
    }

    @Override
    public boolean playerRemoveGroup(String world, String player, String group) {
        return user(player, user -> {
            core.users().modify(user, modifier -> modifier.removeGroup(group, contexts(world))).toCompletableFuture().join();
            return true;
        });
    }

    @Override
    public String[] getPlayerGroups(String world, String player) {
        return find(player).map(user -> core.resolvers().inheritance().groups(user, contexts(world)).stream()
                .map(Group::name).toArray(String[]::new)).orElseGet(() -> new String[0]);
    }

    @Override
    public String getPrimaryGroup(String world, String player) {
        return find(player).flatMap(user -> core.resolvers().primaryGroup().resolve(user, contexts(world)))
                .map(Group::name).orElse(null);
    }

    @Override
    public String[] getGroups() {
        return core.groups().cache().all().stream().map(Group::name).toArray(String[]::new);
    }

    @Override
    public boolean hasGroupSupport() {
        return true;
    }

    private ContextSet contexts(String world) {
        return world == null || world.isBlank() ? ContextSet.empty() : ContextSet.builder().add("world", world).build();
    }

    private Optional<User> find(String name) {
        try {
            return core.users().find(name).toCompletableFuture().join();
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private boolean user(String name, Function<User, Boolean> operation) {
        return find(name).map(operation).orElse(false);
    }
}
