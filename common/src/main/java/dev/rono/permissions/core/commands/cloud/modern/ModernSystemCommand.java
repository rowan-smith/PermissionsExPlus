package dev.rono.permissions.core.commands.cloud.modern;

import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

public final class ModernSystemCommand<C> extends AbstractModernPexCloudCommand<C> {

    public ModernSystemCommand(CoreCloudCommandContext<C> ctx) {
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

    @CommandMethod("pex version")
    public void version(C sender) {
        reply(sender, ctx.commandService().version(ctx.senderAdapter().pluginVersion()));
    }

    @CommandMethod("pex debug")
    public void debugStatus(C sender) {
        reply(sender, ctx.commandService().debugStatus());
    }

    @CommandMethod("pex debug on")
    public void debugOn(C sender) {
        reply(sender, ctx.commandService().setDebug(true));
    }

    @CommandMethod("pex debug off")
    public void debugOff(C sender) {
        reply(sender, ctx.commandService().setDebug(false));
    }
}
