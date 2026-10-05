package ru.tehkode.permissions.backends.data;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexImplProvider;
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
            user.groups().stream().filter(node -> node.contexts().equals(contexts)).forEach(modifier::removeGroup);
            parents.forEach(value -> modifier.addGroup(value, contexts));
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
        return find(identifier).isEmpty();
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
