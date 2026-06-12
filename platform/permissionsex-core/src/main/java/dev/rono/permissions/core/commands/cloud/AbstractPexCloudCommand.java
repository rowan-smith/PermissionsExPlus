package dev.rono.permissions.core.commands.cloud;

import java.util.List;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

/**
 * Shared helpers for Cloud annotation-based PEX handlers.
 *
 * @param <C> command sender type
 */
abstract class AbstractPexCloudCommand<C> {

    protected final CoreCloudCommandContext<C> ctx;

    protected AbstractPexCloudCommand(CoreCloudCommandContext<C> ctx) {
        this.ctx = ctx;
    }

    protected final void reply(C sender, String message) {
        ctx.senderAdapter().reply(sender, message);
    }

    protected final void replyLines(C sender, List<String> lines) {
        for (String line : lines) {
            reply(sender, line);
        }
    }

    protected final String defaultWorld(C sender) {
        return ctx.senderAdapter().defaultWorld(sender);
    }
}
