package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.rono.permissions.core.PexApiImpl;

import java.util.function.BiConsumer;
import dev.rono.permissions.core.util.Immutables;

@CommandMethod("pex backend")
public final class BackendCommands<C> extends CommandSupport<C> {
    BackendCommands(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        super(core, messages);
    }

    @CommandMethod("")
    @CommandPermission("pex.backend.info")
    public void backend(C sender) {
        info(sender);
    }

    @CommandMethod("info")
    @CommandPermission("pex.backend.info")
    public void info(C sender) {
        var backend = core.backend().current();

        heading(sender, "Backend", backend.name());
        section(sender, "Properties", Immutables.listOf("persistent:" + backend.persistent(), "status:" + backend.status()));
    }

    @CommandMethod("list")
    @CommandPermission("pex.backend.list")
    public void list(C sender) {
        var current = core.backend().current();

        heading(sender, "Backends", Integer.toString(core.backend().available().size()));
        section(sender, "Available", core.backend().available().stream()
                .map(value -> value.name() + (value.name().equalsIgnoreCase(current.name()) ? " (active)" : ""))
                .collect(Immutables.toList()));
    }

    @CommandMethod("switch <backend>")
    @CommandPermission("pex.backend.switch")
    public void switchBackend(C sender, @Argument(value = "backend", suggestions = "backends") String name) {
        var backend = core.backend().available().stream().filter(value -> value.name().equalsIgnoreCase(name))
                .findFirst();

        if (!backend.isPresent()) {
            reply(sender, "§cUnknown backend '" + name + "'. Use §f/pex backend list§c.");
            return;
        }

        if (backend.get().name().equalsIgnoreCase(core.backend().current().name())) {
            reply(sender, "§aBackend §f" + backend.get().name() + " §ais already active.");
            return;
        }

        reply(sender, "§eSet §ftype: \"" + backend.get().name().toLowerCase(java.util.Locale.ROOT) + "\" §ein database.yml, then restart the server.");
    }
}
