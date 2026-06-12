package dev.rono.permissions.core.commands.cloud;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import java.util.Arrays;
import java.util.List;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

/**
 * Proxy-only mirrors of hierarchy / inheritance — uses {@code pex server}/{@code servers} instead of {@code world}/{@code worlds}.
 */
public final class ProxyServerSubtreeCommand<C> extends AbstractPexCloudCommand<C> {

    public ProxyServerSubtreeCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex hierarchy <server>")
    public void hierarchyServer(C sender, @Argument(value = "server", suggestions = "pex-server") String server) {
        replyLines(sender, ctx.commandService().hierarchyLines(server));
    }

    @CommandMethod("pex servers")
    public void serversList(C sender) {
        replyLines(sender, ctx.commandService().worldsTreeLines());
    }

    @CommandMethod("pex server <server>")
    public void serverInherit(C sender, @Argument(value = "server", suggestions = "pex-server") String server) {
        replyLines(sender, ctx.commandService().worldInheritanceLines(server));
    }

    @CommandMethod("pex server <server> inherit <parents>")
    public void serverInheritSet(
            C sender,
            @Argument(value = "server", suggestions = "pex-server") String server,
            @Argument(value = "parents", suggestions = "pex-server") String parents) {
        List<String> list = Arrays.stream(parents.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        reply(sender, ctx.commandService().setWorldInheritance(server, list));
    }
}
