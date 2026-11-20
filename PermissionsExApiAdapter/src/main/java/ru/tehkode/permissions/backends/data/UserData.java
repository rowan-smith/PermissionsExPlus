package ru.tehkode.permissions.backends.data;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionHolderModifier;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.core.PexImplProvider;
import dev.rono.permissions.core.util.Immutables;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
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

        api.users().modify(user, modifier -> replacePermanent(modifier, user, contexts, permissions)).toCompletableFuture().join();
    }

    @Override
    protected void modifyPermissions(Consumer<PermissionHolderModifier<?>> action) {
        PexImplProvider.get().users().modify(user(), action::accept).toCompletableFuture().join();
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
            // An empty value clears the option, as setPrefix("") does in PermissionsEx.
            if (value == null || value.isEmpty()) {
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

            if (id.equals(current.uniqueId())) {
                identifier = value;
                return true;
            }

            // Not loadOrCreateUser: it returns the existing user with this name, which is
            // the one being converted, and the conversion would then delete it. Names are
            // unique, so the new user takes the name once the old one is gone.
            var replacement = api.users().find(id).toCompletableFuture().join()
                    .orElseGet(() -> api.users().create(id).toCompletableFuture().join());

            api.users().modify(replacement.uniqueId(), modifier -> {
                current.explicitPermissions().forEach(modifier::setPermission);
                current.explicitOptions().forEach(modifier::setOption);
                modifier.setGroups(current.groups());
            }).toCompletableFuture().join();

            api.users().delete(current.uniqueId()).toCompletableFuture().join();
            api.users().modify(replacement.uniqueId(), modifier -> modifier.updateName(current.name())).toCompletableFuture().join();
            identifier = value;
            return true;
        } catch (IllegalArgumentException ignored) {
            api.users().modify(current.uniqueId(), modifier -> modifier.updateName(value)).toCompletableFuture().join();
            identifier = value;
            return true;
        }
    }
}
