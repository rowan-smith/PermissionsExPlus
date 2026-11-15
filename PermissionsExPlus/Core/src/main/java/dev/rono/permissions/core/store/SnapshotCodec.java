package dev.rono.permissions.core.store;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.exception.StorageException;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionValue;
import dev.rono.permissions.core.model.GroupSnapshot;
import dev.rono.permissions.core.model.LadderSnapshot;
import dev.rono.permissions.core.model.UserSnapshot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import dev.rono.permissions.core.util.Immutables;
import dev.rono.permissions.core.util.Strings;

public final class SnapshotCodec {
    private SnapshotCodec() {}

    public static String user(UserSnapshot user) {
        var root = node();

        set(root.node("id"), user.uniqueId().toString());
        set(root.node("name"), user.name());

        writePermissions(root.node("permissions"), user.explicitPermissions());
        writeOptions(root.node("options"), user.explicitOptions());
        writeParents(root.node("groups"), user.groups());

        return save(root);
    }

    public static UserSnapshot user(String payload) {
        var root = load(payload);

        return new UserSnapshot(UUID.fromString(Objects.requireNonNull(root.node("id").getString())), root.node("name").getString(), readPermissions(root.node("permissions")), readOptions(root.node("options")), readParents(root.node("groups")));
    }

    public static String group(GroupSnapshot group) {
        var root = node();

        set(root.node("name"), group.name());

        if (group.weight().isPresent()) {
            set(root.node("weight"), group.weight().getAsInt());
        }

        writePermissions(root.node("permissions"), group.explicitPermissions());
        writeOptions(root.node("options"), group.explicitOptions());
        writeParents(root.node("parents"), group.parents());

        return save(root);
    }

    public static GroupSnapshot group(String payload) {
        var root = load(payload);

        var weight = root.node("weight").virtual() ? OptionalInt.empty() : OptionalInt.of(root.node("weight").getInt());

        return new GroupSnapshot(root.node("name").getString(), weight, readPermissions(root.node("permissions")), readOptions(root.node("options")), readParents(root.node("parents")));
    }

    public static String ladder(LadderSnapshot ladder) {
        var root = node();

        set(root.node("name"), ladder.name());
        set(root.node("groups"), ladder.groups());

        return save(root);
    }

    public static LadderSnapshot ladder(String payload) {
        var root = load(payload);

        try {
            return new LadderSnapshot(root.node("name").getString(),
                    root.node("groups").getList(String.class, Immutables.listOf()));
        } catch (Exception error) {
            throw storage(error);
        }
    }

    public static String context(ContextSet contexts) {
        var root = node();

        writeContexts(root, contexts);

        return save(root);
    }

    public static ContextSet context(String payload) {
        return readContexts(load(payload));
    }

    private static void writePermissions(ConfigurationNode target, Set<PermissionNode> values) {
        int index = 0;

        for (var value : values) {
            var node = target.node(index++);

            set(node.node("permission"), value.permission());
            set(node.node("value"), value.value().name());

            writeContexts(node.node("contexts"), value.contexts());

            value.expiry().ifPresent(expiry -> set(node.node("expiry"), expiry.toString()));
        }
    }

    private static Set<PermissionNode> readPermissions(ConfigurationNode target) {
        var values = new LinkedHashSet<PermissionNode>();

        for (var node : target.childrenList()) {
            var builder = PermissionNode.builder().permission(node.node("permission").getString())
                    .value(PermissionValue.valueOf(node.node("value").getString("ALLOW")))
                    .contexts(readContexts(node.node("contexts")));

            expiry(node).ifPresent(builder::expiry);

            values.add(builder.build());
        }

        return values;
    }

    private static void writeOptions(ConfigurationNode target, Set<OptionNode> values) {
        int index = 0;

        for (var value : values) {
            var node = target.node(index++);

            set(node.node("key"), value.key());
            set(node.node("value"), value.value());

            writeContexts(node.node("contexts"), value.contexts());

            value.expiry().ifPresent(expiry -> set(node.node("expiry"), expiry.toString()));
        }
    }

    private static Set<OptionNode> readOptions(ConfigurationNode target) {
        var values = new LinkedHashSet<OptionNode>();

        for (var node : target.childrenList()) {
            var builder = OptionNode.builder().key(node.node("key").getString()).value(node.node("value").getString()).contexts(readContexts(node.node("contexts")));

            expiry(node).ifPresent(builder::expiry);

            values.add(builder.build());
        }

        return values;
    }

    private static void writeParents(ConfigurationNode target, Set<ParentNode> values) {
        int index = 0;

        for (var value : values) {
            var node = target.node(index++);

            set(node.node("group"), value.group());

            writeContexts(node.node("contexts"), value.contexts());

            value.expiry().ifPresent(expiry -> set(node.node("expiry"), expiry.toString()));
        }
    }

    private static Set<ParentNode> readParents(ConfigurationNode target) {
        var values = new LinkedHashSet<ParentNode>();

        for (var node : target.childrenList()) {
            var builder = ParentNode.builder().group(node.node("group").getString()).contexts(readContexts(node.node("contexts")));

            expiry(node).ifPresent(builder::expiry);

            values.add(builder.build());
        }

        return values;
    }

    private static void writeContexts(ConfigurationNode target, ContextSet contexts) {
        contexts.asMap().forEach((key, values) -> set(target.node(key), new ArrayList<>(values)));
    }

    private static ContextSet readContexts(ConfigurationNode target) {
        var builder = ContextSet.builder();

        target.childrenMap().forEach((key, value) -> {
            try {
                value.getList(String.class, Immutables.listOf()).forEach(entry -> builder.add(String.valueOf(key), entry));
            } catch (Exception error) {
                throw storage(error);
            }
        });

        return builder.build();
    }

    private static Optional<Instant> expiry(ConfigurationNode node) {
        var value = node.node("expiry").getString("");

        try {
            return Strings.isBlank(value) ? Optional.empty() : Optional.of(Instant.parse(value));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static ConfigurationNode node() {
        return GsonConfigurationLoader.builder().build().createNode();
    }

    private static String save(ConfigurationNode node) {
        try {
            return GsonConfigurationLoader.builder().buildAndSaveString(node);
        } catch (Exception error) {
            throw storage(error);
        }
    }

    private static ConfigurationNode load(String payload) {
        try {
            return GsonConfigurationLoader.builder().buildAndLoadString(payload);
        } catch (Exception error) {
            throw storage(error);
        }
    }

    private static void set(ConfigurationNode node, Object value) {
        try {
            node.set(value);
        } catch (Exception error) {
            throw storage(error);
        }
    }

    private static StorageException storage(Throwable error) {
        return new StorageException("Unable to encode or decode API data", error);
    }
}
