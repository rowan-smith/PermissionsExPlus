package dev.rono.permissions.core.command;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.rono.permissions.core.PexApiImpl;

import java.util.function.BiConsumer;
import dev.rono.permissions.core.util.Immutables;

@CommandMethod("pex")
public final class RootCommands<C> extends CommandSupport<C> {
    RootCommands(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        super(core, messages);
    }

    @CommandMethod("")
    public void root(C sender) {
        heading(sender, "PermissionsExPlus", "API Commands");
        section(sender, "Commands", Immutables.listOf("/pex user <user>", "/pex group <group>", "/pex ladder <ladder>", "/pex worlds", "/pex backend", "/pex import permissionsex [folder]", "/pex reload"));
    }

    @CommandMethod("help")
    @CommandPermission("pex.admin")
    public void help(C sender) {
        root(sender);
    }

    @CommandMethod("reload")
    @CommandPermission("pex.reload")
    public void reload(C sender) {
        core.reload();

        audit(sender, "RELOADED PermissionsExPlus");

        reply(sender, "§aPermissionsExPlus reloaded.");
    }

    @CommandMethod("version")
    @CommandPermission("pex.version")
    public void version(C sender) {
        heading(sender, "PermissionsExPlus", "API");
    }

    @CommandMethod("debug")
    @CommandPermission("pex.debug")
    public void debug(C sender) {
        reply(sender, "§eUsage: /pex debug [on|off]");
    }

    @CommandMethod("debug on")
    @CommandPermission("pex.debug.on")
    public void debugOn(C sender) {
        reply(sender, "§aDebug mode enabled for configuration-driven verbose logging.");
    }

    @CommandMethod("debug off")
    @CommandPermission("pex.debug.off")
    public void debugOff(C sender) {
        reply(sender, "§aDebug mode disabled for configuration-driven verbose logging.");
    }
}
