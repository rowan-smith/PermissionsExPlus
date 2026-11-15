package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import dev.rono.permissions.api.backend.Backend;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionKeys;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexApiImpl;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import dev.rono.permissions.core.util.Immutables;
import dev.rono.permissions.core.util.Optionals;

@CommandMethod("pex")
public final class CommandSuggestions<C> extends CommandSupport<C> {
    CommandSuggestions(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        super(core, messages);
    }

    @Suggestions("users")
    public List<String> users(CommandContext<C> context, String input) {
        return matching(core.users().cache().all().stream().map(User::name), input);
    }

    @Suggestions("groups")
    public List<String> groups(CommandContext<C> context, String input) {
        return matching(core.groups().cache().identifiers().stream(), input);
    }

    @Suggestions("ladders")
    public List<String> ladders(CommandContext<C> context, String input) {
        return matching(core.ladders().cache().identifiers().stream(), input);
    }

    @Suggestions("backends")
    public List<String> backends(CommandContext<C> context, String input) {
        return matching(core.backend().available().stream().map(Backend::name), input);
    }

    @Suggestions("permissions")
    public List<String> permissions(CommandContext<C> context, String input) {
        return matching(knownPermissions(), input);
    }

    @Suggestions("user-permissions-add")
    public List<String> userPermissionsAdd(CommandContext<C> context, String input) {
        var contexts = contexts(context);

        return matching(user(context).map(user -> knownPermissions()
                .filter(permission -> !user.explicitPermission(permission, contexts).isPresent()))
                .orElseGet(Stream::empty), input);
    }

    @Suggestions("user-permissions-remove")
    public List<String> userPermissionsRemove(CommandContext<C> context, String input) {
        var contexts = contexts(context);

        return matching(Optionals.stream(user(context)).flatMap(user -> user.explicitPermissions().stream())
                .filter(permission -> permission.contexts().equals(contexts))
                .map(PermissionNode::permission), input);
    }

    @Suggestions("user-groups-add")
    public List<String> userGroupsAdd(CommandContext<C> context, String input) {
        var contexts = contexts(context);

        return matching(user(context).map(user -> await(core.groups().storage().identifiers()).stream()
                .filter(group -> !user.hasDirectGroup(group, contexts))).orElseGet(Stream::empty), input);
    }

    @Suggestions("user-groups-remove")
    public List<String> userGroupsRemove(CommandContext<C> context, String input) {
        var contexts = contexts(context);

        return matching(Optionals.stream(user(context)).flatMap(user -> user.groups().stream())
                .filter(group -> group.contexts().equals(contexts)).map(ParentNode::group), input);
    }

    private Stream<String> knownPermissions() {
        var users = core.users().cache().all().stream().flatMap(user -> user.explicitPermissions().stream())
                .map(PermissionNode::permission);

        var groups = core.groups().cache().all().stream().flatMap(group -> group.explicitPermissions().stream())
                .map(PermissionNode::permission);

        return Stream.concat(Stream.of("*"), Stream.concat(users, groups));
    }

    @Suggestions("options")
    public List<String> options(CommandContext<C> context, String input) {
        return matching(Stream.of(OptionKeys.PREFIX, OptionKeys.SUFFIX, OptionKeys.DISPLAY_NAME, "weight", "default"),
                input);
    }

    private List<String> matching(Stream<String> values, String input) {
        var prefix = input.toLowerCase(Locale.ROOT);

        return values.distinct().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix)).sorted().collect(Immutables.toList());
    }

    private Optional<User> user(CommandContext<C> context) {
        return context.<String>getOptional("user").flatMap(name -> await(core.users().find(name)));
    }

    private ContextSet contexts(CommandContext<C> context) {
        return context.getOrDefault(PosixContextFlagExtractor.CONTEXTS_KEY, ContextSet.empty());
    }
}
