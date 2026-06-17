package dev.rono.permissions.core.commands.cloud.classic;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

public final class ClassicSystemCommand<C> extends AbstractClassicPexCloudCommand<C> {

    public ClassicSystemCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex reload")
    public void reload(C sender) {
        try {
            reply(sender, ctx.commandService().reload(ctx.configReloader()));
        } catch (Exception ex) {
            reply(sender, "Reload failed: " + ex.getMessage());
        }
    }

    @CommandMethod("pex backend")
    public void backendShow(C sender) {
        reply(sender, ctx.commandService().currentBackend());
    }

    @CommandMethod("pex backend <backend>")
    public void backendSet(C sender, @Argument(value = "backend", suggestions = "pex-backend") String backend) {
        try {
            reply(sender, ctx.commandService().switchBackend(backend));
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof ClassNotFoundException) {
                reply(sender, "Specified backend not found.");
            } else {
                reply(sender, "Error during backend initialization.");
            }
        } catch (PermissionBackendException ex) {
            reply(sender, "Backend initialization failed! Fix your configuration!\nError: " + ex.getMessage());
        } catch (Exception ex) {
            reply(sender, "Failed to switch backend: " + ex.getMessage());
        }
    }

    @CommandMethod("pex config <node>")
    public void configGet(C sender, @Argument("node") String node) {
        try {
            replyLines(sender, ctx.commandService().configNodeLines(ctx.configBridge(), node, null));
        } catch (Exception ex) {
            reply(sender, "Config command failed: " + ex.getMessage());
        }
    }

    @CommandMethod("pex config <node> <value>")
    public void configSet(C sender, @Argument("node") String node, @Argument("value") @Greedy String value) {
        try {
            replyLines(sender, ctx.commandService().configNodeLines(ctx.configBridge(), node, value));
        } catch (Exception ex) {
            reply(sender, "Config command failed: " + ex.getMessage());
        }
    }

    @CommandMethod("pex convert uuid")
    public void uuidConvert(C sender) {
        reply(sender, ctx.commandService().convertUuid(ctx.uuidConversionBridge(), false));
    }

    @CommandMethod("pex convert uuid force")
    public void uuidConvertForce(C sender) {
        reply(sender, ctx.commandService().convertUuid(ctx.uuidConversionBridge(), true));
    }

    @CommandMethod("pex toggle debug")
    public void toggleDebug(C sender) {
        reply(sender, ctx.commandService().toggleDebug());
    }

    @CommandMethod("pex version")
    public void version(C sender) {
        reply(sender, ctx.commandService().version(ctx.senderAdapter().pluginVersion()));
    }

    @CommandMethod("pex import <backend>")
    public void importBackend(C sender, @Argument(value = "backend", suggestions = "pex-backend") String backend) {
        try {
            reply(sender, ctx.commandService().importDataFromBackend(backend, ctx.importBridge()));
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof ClassNotFoundException) {
                reply(sender, "Specified backend not found!");
            } else {
                reply(sender, "Error: " + ex.getMessage());
            }
        } catch (PermissionBackendException ex) {
            reply(sender, "Backend " + backend + " was unable to load due to user configuration error. See console for details.");
        }
    }
}
