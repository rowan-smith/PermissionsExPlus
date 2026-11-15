package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexApiImpl;

import java.util.function.BiConsumer;
import dev.rono.permissions.core.util.Immutables;

@CommandMethod("pex users")
public final class UsersCommands<C> extends CommandSupport<C> {
    UsersCommands(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        super(core, messages);
    }

    @CommandMethod("")
    @CommandPermission("pex.users.list")
    public void list(C sender) {
        section(sender, "Cached users", core.users().cache().all().stream().map(User::name).collect(Immutables.toList()));
    }

    @CommandMethod("create <name>")
    @CommandPermission("pex.users.create")
    public void create(C sender, @Argument("name") String name) {
        core.resolveUuidAsync(name, id -> {
            if (!id.isPresent()) {
                reply(sender, "§cCould not resolve a UUID for §f" + name);
                return;
            }

            core.users().createUserAsync(id.get(), name, (user, created) -> {
                reply(sender, created ? "§aCreated user §f" + user.name() : "§eUser §f" + user.name() + " §ealready exists");
            });
        });
    }

    @CommandMethod("delete <user>")
    @CommandPermission("pex.users.delete")
    public void delete(C sender, @Argument(value = "user", suggestions = "users") String name) {
        var user = await(core.users().find(name)).orElse(null);
        if (user == null) {
            reply(sender, "§cUser not found: " + name);
            return;
        }

        await(core.users().delete(user.uniqueId()));

        reply(sender, "§aDeleted user §f" + user.name());
    }
}
