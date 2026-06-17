package dev.rono.permissions.core.commands.cloud.modern;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

public final class ModernBackendCommand<C> extends AbstractModernPexCloudCommand<C> {

    public ModernBackendCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex backend")
    public void backendRoot(C sender) {
        reply(sender, ctx.commandService().currentBackend());
    }

    @CommandMethod("pex backend info")
    public void backendInfo(C sender) {
        reply(sender, ctx.commandService().currentBackend());
    }

    @CommandMethod("pex backend list")
    public void backendList(C sender) {
        replyLines(sender, ctx.commandService().backendListLines());
    }

    @CommandMethod("pex backend switch <backend>")
    public void backendSwitch(C sender, @Argument(value = "backend", suggestions = "pex-backend") String backend) {
        try {
            reply(sender, ctx.commandService().switchBackend(backend));
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof ClassNotFoundException) {
                reply(sender, "Specified backend not found.");
            } else {
                reply(sender, "Error during backend initialization: " + ex.getMessage());
            }
        } catch (PermissionBackendException ex) {
            reply(sender, "Backend initialization failed. Fix your configuration.\nError: " + ex.getMessage());
        } catch (Exception ex) {
            reply(sender, "Failed to switch backend: " + ex.getMessage());
        }
    }

    @CommandMethod("pex backend import <backend>")
    public void backendImport(C sender, @Argument(value = "backend", suggestions = "pex-backend") String backend) {
        try {
            reply(sender, ctx.commandService().importDataFromBackend(backend, ctx.importBridge()));
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof ClassNotFoundException) {
                reply(sender, "Specified backend not found.");
            } else {
                reply(sender, "Error during backend initialization: " + ex.getMessage());
            }
        } catch (PermissionBackendException ex) {
            reply(sender, "Backend \"" + backend + "\" failed to load due to a configuration error.");
        } catch (Exception ex) {
            reply(sender, "Import failed: " + ex.getMessage());
        }
    }

    @CommandMethod("pex backend export")
    public void backendExport(C sender) {
        try {
            reply(sender, ctx.commandService().backendExport());
        } catch (PermissionBackendException ex) {
            reply(sender, "Export failed: " + ex.getMessage());
        }
    }

    @CommandMethod("pex backend export <backend>")
    public void backendExportNamed(
            C sender, @Argument(value = "backend", suggestions = "pex-backend") String backend) {
        try {
            reply(sender, ctx.commandService().backendExport(backend));
        } catch (PermissionBackendException ex) {
            reply(sender, "Export failed: " + ex.getMessage());
        }
    }
}
