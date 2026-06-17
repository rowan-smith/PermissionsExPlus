package dev.rono.permissions.core.commands.cloud.modern;

import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

public final class ModernRootCommand<C> extends AbstractModernPexCloudCommand<C> {

    public ModernRootCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex")
    public void pexHelp(C sender) {
        reply(sender, ctx.senderAdapter().helpText());
    }

    @CommandMethod("pex help")
    public void helpRoot(C sender) {
        reply(sender, ctx.senderAdapter().helpText());
    }

    @CommandMethod("pex help <page>")
    public void helpPage(C sender) {
        reply(sender, "Paginated help is not available. Use /pex help for the command summary.");
    }

    @CommandMethod("pex help <page> <count>")
    public void helpPaged(C sender) {
        reply(sender, "Paginated help is not available. Use /pex help for the command summary.");
    }

    @CommandMethod("pex report")
    public void report(C sender) {
        reply(sender, ctx.senderAdapter().reportText());
    }
}
