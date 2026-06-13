package dev.rono.permissions.core.commands.cloud.modern;

import dev.rono.permissions.core.commands.CoreCloudCommandContext;

import java.util.List;

abstract class AbstractModernPexCloudCommand<C> {

    protected final CoreCloudCommandContext<C> ctx;

    protected AbstractModernPexCloudCommand(CoreCloudCommandContext<C> ctx) {
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

    protected final String realm(C sender, PexCommandFlags flags) {
        return ModernCommandSupport.storageRealm(ctx, sender, flags);
    }

    protected final void replyError(C sender, Exception ex) {
        reply(sender, ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
    }
}
