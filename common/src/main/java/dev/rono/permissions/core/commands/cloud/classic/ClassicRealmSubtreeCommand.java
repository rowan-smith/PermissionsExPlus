package dev.rono.permissions.core.commands.cloud.classic;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

import java.util.Arrays;
import java.util.List;

/**
 * World/server hierarchy commands. Game servers register {@link Game}; proxies register {@link Proxy}.
 */
public final class ClassicRealmSubtreeCommand {
    private ClassicRealmSubtreeCommand() {}

    /**
     * {@code pex worlds}, {@code pex hierarchy <world>}, and {@code pex world <world>} — game servers only.
     */
    public static final class Game<C> extends AbstractClassicPexCloudCommand<C> {

        public Game(CoreCloudCommandContext<C> ctx) {
            super(ctx);
        }

        @CommandMethod("pex worlds")
        public void worlds(C sender) {
            replyLines(sender, ctx.commandService().worldsTreeLines());
        }

        @CommandMethod("pex hierarchy <world>")
        public void hierarchy(C sender, @Argument(value = "world", suggestions = "pex-world") String world) {
            replyLines(sender, ctx.commandService().hierarchyLines(world));
        }

        @CommandMethod("pex world <world>")
        public void worldInherit(C sender, @Argument(value = "world", suggestions = "pex-world") String world) {
            replyLines(sender, ctx.commandService().worldInheritanceLines(world));
        }

        @CommandMethod("pex world <world> inherit <parents>")
        public void worldInheritSet(
                C sender,
                @Argument(value = "world", suggestions = "pex-world") String world,
                @Argument(value = "parents", suggestions = "pex-world") String parents) {
            List<String> list = Arrays.stream(parents.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
            reply(sender, ctx.commandService().setWorldInheritance(world, list));
        }
    }

    /**
     * Proxy-only mirrors — uses {@code pex server}/{@code servers} instead of {@code world}/{@code worlds}.
     */
    public static final class Proxy<C> extends AbstractClassicPexCloudCommand<C> {

        public Proxy(CoreCloudCommandContext<C> ctx) {
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
}
