/*
 * PermissionsEx - Permissions plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ru.tehkode.permissions.bukkit;

import dev.rono.permissions.api.PexApi;
import dev.rono.permissions.api.context.ContextKeys;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.core.PexImplProvider;
import dev.rono.permissions.core.manager.GroupManagerImpl;
import dev.rono.permissions.core.manager.LadderManagerImpl;
import dev.rono.permissions.core.manager.UserManagerImpl;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Thin access to PermissionsExPlus for legacy command handlers.
 */
public final class ApiWrapper {
    private ApiWrapper() {
        throw new AssertionError();
    }

    public static PexApiImpl<?> core() {
        return PexImplProvider.get();
    }

    public static PexApi api() {
        return core();
    }

    public static UserManagerImpl users() {
        return core().users();
    }

    public static GroupManagerImpl groups() {
        return core().groups();
    }

    public static LadderManagerImpl ladders() {
        return core().ladders();
    }

    public static <T> T await(CompletionStage<T> stage) {
        try {
            return stage.toCompletableFuture().get(15, TimeUnit.SECONDS);
        } catch (Exception error) {
            Throwable cause = error.getCause() != null ? error.getCause() : error;
            throw new IllegalStateException(cause.getMessage() != null ? cause.getMessage() : cause.toString(), cause);
        }
    }

    public static ContextSet world(String world) {
        if (world == null || world.isBlank()) {
            return ContextSet.empty();
        }

        return ContextSet.builder().add(ContextKeys.WORLD, world).build();
    }

    public static QueryOptions query(String world) {
        return QueryOptions.builder().contexts(world(world)).build();
    }

    public static Optional<User> findUser(String nameOrUuid) {
        if (nameOrUuid == null || nameOrUuid.isBlank()) {
            return Optional.empty();
        }

        try {
            return await(users().find(UUID.fromString(nameOrUuid)));
        } catch (IllegalArgumentException ignored) {}

        Player online = Bukkit.getPlayerExact(nameOrUuid);

        if (online != null) {
            return Optional.of(await(users().loadOrCreateUser(online.getUniqueId(), online.getName())));
        }

        Optional<User> cached = users().cache().get(nameOrUuid);

        if (cached.isPresent()) {
            return cached;
        }

        Optional<User> found = await(users().find(nameOrUuid));

        if (found.isPresent()) {
            return found;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(nameOrUuid);

        if (offline.getUniqueId() != null && (offline.hasPlayedBefore() || offline.isOnline())) {
            String name = offline.getName() != null ? offline.getName() : nameOrUuid;
            return Optional.of(await(users().loadOrCreateUser(offline.getUniqueId(), name)));
        }

        return Optional.empty();
    }

    public static User requireUser(String nameOrUuid) {
        return findUser(nameOrUuid).orElseThrow(() -> new IllegalArgumentException("User \"" + nameOrUuid + "\" doesn't exist."));
    }

    public static Optional<Group> findGroup(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        return await(groups().find(name));
    }

    public static Group requireGroup(String name) {
        return findGroup(name).orElseThrow(() -> new IllegalArgumentException("Group \"" + name + "\" doesn't exist."));
    }

    public static Group requireOrCreateGroup(String name) {
        return findGroup(name).orElseGet(() -> await(groups().create(name)));
    }
}
