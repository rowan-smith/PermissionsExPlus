package dev.rono.permissions.core.commands.cloud.modern;

import dev.rono.permissions.core.commands.CoreCloudCommandContext;
import dev.rono.permissions.core.commands.CoreCloudCommandRegistrar;
import dev.rono.permissions.core.commands.CoreCloudPlatform;

final class ModernCommandSupport {
    private ModernCommandSupport() {}

    static <C> String storageRealm(CoreCloudCommandContext<C> ctx, C sender, PexCommandFlags flags) {
        CoreCloudCommandRegistrar.SenderAdapter<C> adapter = ctx.senderAdapter();
        if (ctx.cloudPlatform() == CoreCloudPlatform.PROXY) {
            if (flags.get("server").isPresent()) {
                return flags.get("server").get();
            }
            if (flags.get("world").isPresent()) {
                return flags.get("world").get();
            }
        } else {
            if (flags.get("world").isPresent()) {
                return flags.get("world").get();
            }
            if (flags.get("server").isPresent()) {
                return flags.get("server").get();
            }
        }
        return adapter.defaultWorld(sender);
    }

    static String displayRealm(String realm) {
        return realm == null ? "global" : realm;
    }
}
