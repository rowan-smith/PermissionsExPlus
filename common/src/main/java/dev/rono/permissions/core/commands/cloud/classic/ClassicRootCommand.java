package dev.rono.permissions.core.commands.cloud.classic;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

public final class ClassicRootCommand<C> extends AbstractClassicPexCloudCommand<C> {

    public ClassicRootCommand(CoreCloudCommandContext<C> ctx) {
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
    public void helpPage(C sender, @Argument("page") String page) {
        reply(sender, ctx.senderAdapter().helpText());
    }

    @CommandMethod("pex help <page> <count>")
    public void helpPaged(C sender, @Argument("page") String page, @Argument("count") String count) {
        reply(sender, ctx.senderAdapter().helpText());
    }

    @CommandMethod("pex report")
    public void report(C sender) {
        reply(sender, ctx.senderAdapter().reportText());
    }
}
