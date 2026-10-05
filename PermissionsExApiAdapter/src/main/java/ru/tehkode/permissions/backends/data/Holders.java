package ru.tehkode.permissions.backends.data;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexImplProvider;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * Shared identity lookup for the ApiAdapter → PermissionsExPlus bridge.
 * Package-private: not part of the classic PEX public surface.
 */
final class Holders {
    private Holders() {}

    public static ContextSet world(String world) {
        return world == null || world.isBlank() ? ContextSet.empty() : ContextSet.builder().add("world", world).build();
    }

    public static Optional<User> findUser(String identifier) {
        var api = PexImplProvider.get();

        try {
            return api.users().find(UUID.fromString(identifier)).toCompletableFuture().join();
        } catch (IllegalArgumentException ignored) {
            return api.users().find(identifier).toCompletableFuture().join();
        }
    }

    public static User user(String identifier) {
        return findUser(identifier).orElseGet(() -> {
            var api = PexImplProvider.get();
            UUID id;

            try {
                id = UUID.fromString(identifier);
            } catch (IllegalArgumentException ignored) {
                id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + identifier).getBytes(StandardCharsets.UTF_8));
            }

            return api.users().loadOrCreateUser(id, identifier).toCompletableFuture().join();
        });
    }

    public static Optional<Group> findGroup(String identifier) {
        return PexImplProvider.get().groups().find(identifier).toCompletableFuture().join();
    }

    public static Group group(String identifier) {
        var api = PexImplProvider.get();

        return api.groups().cache().get(identifier)
                .or(() -> api.groups().storage().get(identifier).toCompletableFuture().join())
                .orElseGet(() -> api.groups().create(identifier).toCompletableFuture().join());
    }
}
