package ru.tehkode.permissions.backends.data;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.core.PexImplProvider;
import java.util.List;
import java.util.Set;
import ru.tehkode.permissions.PermissionsGroupData;

final class GroupData extends AbstractData implements PermissionsGroupData {
    GroupData(String identifier) {
        super(identifier);
    }

    private Group group() {
        return Holders.group(identifier);
    }

    @Override
    protected PermissionHolder holder() {
        return group();
    }

    @Override
    protected Set<ParentNode> parents() {
        return group().parents();
    }

    @Override
    protected void replacePermissions(ContextSet contexts, List<String> permissions) {
        var api = PexImplProvider.get();
        group();

        api.groups().modify(identifier, modifier -> {
            modifier.clearPermissions(contexts);
            permissions.forEach(value -> modifier.setPermission(PermissionNode
                    .builder().permission(value).contexts(contexts).build()));
        }).toCompletableFuture().join();
    }

    @Override
    protected void replaceParents(ContextSet contexts, List<String> parents) {
        var api = PexImplProvider.get();
        var group = group();

        api.groups().modify(identifier, modifier -> {
            group.parents().stream().filter(node -> node.contexts().equals(contexts)).forEach(modifier::removeParent);
            parents.forEach(value -> modifier.addParent(value, contexts));
        }).toCompletableFuture().join();
    }

    @Override
    protected void setOptionNode(ContextSet contexts, String key, String value) {
        var api = PexImplProvider.get();
        group();

        if ("weight".equalsIgnoreCase(key) && contexts.isEmpty()) {
            api.groups().modify(identifier, modifier -> {
                if (value == null) {
                    modifier.clearWeight();
                } else {
                    modifier.setWeight(Integer.parseInt(value));
                }
            }).toCompletableFuture().join();
            return;
        }

        if ("default".equalsIgnoreCase(key)) {
            var configured = api.resolvers().defaultGroups().resolve()
                    .map(group -> group.name().equalsIgnoreCase(identifier)).orElse(false);

            if (Boolean.parseBoolean(value) != configured) {
                throw new UnsupportedOperationException(
                        "The implicit default group is configured by default-group in config.yml");
            }

            return;
        }

        api.groups().modify(identifier, modifier -> {
            if (value == null) {
                modifier.removeOption(key, contexts);
            } else {
                modifier.setOption(key, value, contexts);
            }
        }).toCompletableFuture().join();
    }

    @Override
    public String getOption(String option, String world) {
        if ("weight".equalsIgnoreCase(option) && contexts(world).isEmpty()) {
            return group().weight().isPresent() ? Integer.toString(group().weight().getAsInt()) : null;
        }

        if ("default".equalsIgnoreCase(option)) {
            var api = PexImplProvider.get();

            return Boolean.toString(api.resolvers().defaultGroups().resolve()
                    .map(value -> value.name().equalsIgnoreCase(identifier)).orElse(false));
        }

        return super.getOption(option, world);
    }

    @Override
    public boolean isVirtual() {
        return Holders.findGroup(identifier).isEmpty();
    }

    @Override
    public void remove() {
        PexImplProvider.get().groups().delete(identifier).toCompletableFuture().join();
    }
}
