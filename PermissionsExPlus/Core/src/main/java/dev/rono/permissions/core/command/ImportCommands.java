package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.core.migration.LegacyPexImporter;

import java.util.function.BiConsumer;

/** Imports data from other permission plugins. */
@CommandMethod("pex import")
public final class ImportCommands<C> extends CommandSupport<C> {
    ImportCommands(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        super(core, messages);
    }

    /**
     * Imports an original PermissionsEx installation; by default the
     * PermissionsEx folder beside this plugin's own.
     */
    @CommandMethod("permissionsex [folder]")
    @CommandPermission("pex.import")
    public void permissionsEx(C sender, @Argument("folder") @Greedy String folder) {
        LegacyPexImporter.importInstallation(core, folder, message -> reply(sender, message), source -> audit(sender, "IMPORTED PermissionsEx data from " + source));
    }
}
