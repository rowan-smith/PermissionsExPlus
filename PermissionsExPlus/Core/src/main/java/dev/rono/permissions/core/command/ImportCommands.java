package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.core.migration.LegacyPexImporter;
import dev.rono.permissions.core.migration.LegacyPexReader;

import java.nio.file.Paths;
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
        var path = folder == null ? core.dataDirectory().toAbsolutePath().getParent().resolve("PermissionsEx") : Paths.get(folder);

        reply(sender, "§7Importing PermissionsEx data from §f" + path + "§7...");

        core.runAsync(() -> {
            try {
                var source = LegacyPexReader.readInstallation(path);

                reply(sender, "§7Read §f" + source.data.groupCount() + " §7groups and §f" + source.data.userCount() + " §7users from §f" + source.description);

                var report = LegacyPexImporter.apply(core, source.data);

                audit(sender, "IMPORTED PermissionsEx data from " + source.description);

                reply(sender, "§aImported PermissionsEx data:");
                reply(sender, "§7- Groups: §f" + report.groupsCreated + " §7created, §f" + report.groupsUpdated + " §7already existed");
                reply(sender, "§7- Users: §f" + report.usersImported);
                reply(sender, "§7- Permissions: §f" + report.permissions + "§7, options: §f" + report.options);
                reply(sender, "§7- Group memberships: §f" + report.memberships + " §7(" + report.timedMemberships + " timed; "
                        + report.expiredMemberships + " already expired and skipped)");
                reply(sender, "§7- Ladders: §f" + report.ladders + "§7, world inheritance: §f" + report.worlds);
                report.warnings.forEach(warning -> reply(sender, "§e! " + warning));
            } catch (Exception error) {
                core.logger().error("PermissionsEx import from " + path + " failed", error);
                reply(sender, "§cImport failed: " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage()));
            }
        });
    }
}
