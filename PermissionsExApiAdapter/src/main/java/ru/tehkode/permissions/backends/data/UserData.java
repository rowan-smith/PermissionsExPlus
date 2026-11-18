package ru.tehkode.permissions.backends.data;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.core.PexImplProvider;
import dev.rono.permissions.core.util.Immutables;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import ru.tehkode.permissions.PermissionsUserData;

final class UserData extends AbstractData implements PermissionsUserData {
    UserData(String identifier) {
        super(identifier);
    }

    static Optional<User> find(String identifier) {
        return Holders.findUser(identifier);
    }

    private User user() {
        return Holders.user(identifier);
    }

    @Override
    protected PermissionHolder holder() {
        return user();
    }

    @Override
    protected Set<ParentNode> parents() {
        return user().groups();
    }

    @Override
    protected void replacePermissions(ContextSet contexts, List<String> permissions) {
        var api = PexImplProvider.get();
        var user = user();

        api.users().modify(user, modifier -> {
            modifier.clearPermissions(contexts);
            permissions.forEach(value -> modifier.setPermission(PermissionNode
                    .builder().permission(value).contexts(contexts).build()));
        }).toCompletableFuture().join();
    }

    @Override
    protected void replaceParents(ContextSet contexts, List<String> parents) {
        var api = PexImplProvider.get();
        var user = user();

        api.users().modify(user, modifier -> {
            var existing = user.groups().stream().filter(node -> node.contexts().equals(contexts)).collect(Immutables.toList());

            existing.forEach(modifier::removeGroup);
            // Keep each remaining membership's own node, so a timed membership keeps its
            // expiry.
            parents.forEach(value -> modifier.addGroup(existing.stream().filter(node -> node.group().equals(Identifiers.group(value))).findFirst()
                    .orElseGet(() -> ParentNode.builder().group(value).contexts(contexts).build())));
        }).toCompletableFuture().join();
    }

    /**
     * Adds a membership that expires at {@code expiry}, with the PermissionsEx
     * {@code group-<name>-until} option expiring alongside it for plugins that read
     * it.
     */
    void addTimedGroup(String group, String world, Instant expiry) {
        var api = PexImplProvider.get();
        var user = user();
        var contexts = contexts(world);

        api.users().modify(user, modifier -> {
            modifier.addGroup(ParentNode.builder().group(group).contexts(contexts).expiry(expiry).build());
            modifier.setOption(OptionNode.builder().key("group-" + group + "-until").value(Long.toString(expiry.getEpochSecond()))
                    .contexts(contexts).expiry(expiry).build());
        }).toCompletableFuture().join();
    }

    @Override
    protected void setOptionNode(ContextSet contexts, String key, String value) {
        var api = PexImplProvider.get();
        var user = user();

        api.users().modify(user, modifier -> {
            if (value == null) {
                modifier.removeOption(key, contexts);
            } else {
                modifier.setOption(key, value, contexts);
            }

            if ("name".equalsIgnoreCase(key) && value != null && contexts.isEmpty()) {
                modifier.updateName(value);
            }
        }).toCompletableFuture().join();
    }

    @Override
    public boolean isVirtual() {
        return !find(identifier).isPresent();
    }

    @Override
    public void remove() {
        var api = PexImplProvider.get();

        find(identifier).ifPresent(user -> api.users().delete(user.uniqueId()).toCompletableFuture().join());
    }

    @Override
    public boolean setIdentifier(String value) {
        if (value.equals(identifier)) {
            return true;
        }

        var api = PexImplProvider.get();
        var current = find(identifier).orElse(null);

        if (current == null || find(value).isPresent()) {
            return false;
        }

        try {
            var id = UUID.fromString(value);
            var replacement = api.users().loadOrCreateUser(id, current.name()).toCompletableFuture().join();

            api.users().modify(replacement.uniqueId(), modifier -> {
                current.explicitPermissions().forEach(modifier::setPermission);
                current.explicitOptions().forEach(modifier::setOption);
                modifier.setGroups(current.groups());
            }).toCompletableFuture().join();

            api.users().delete(current.uniqueId()).toCompletableFuture().join();
            identifier = value;
            return true;
        } catch (IllegalArgumentException ignored) {
            api.users().modify(current.uniqueId(), modifier -> modifier.updateName(value)).toCompletableFuture().join();
            identifier = value;
            return true;
        }
    }
}
