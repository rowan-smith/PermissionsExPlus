package dev.rono.permissions.core.commands.cloud.modern;

import dev.rono.permissions.core.commands.CoreCloudCommandContext;
import dev.rono.permissions.core.commands.CoreCloudCommandRegistrar;
import dev.rono.permissions.core.commands.CoreCloudPlatform;
import dev.rono.permissions.core.commands.CoreCommandService;
import dev.rono.permissions.core.config.CommandFramework;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModernCommandSupportTest {

    @Test
    void proxyPrefersServerFlag() {
        CoreCloudCommandContext<String> ctx = context(CoreCloudPlatform.PROXY, "lobby");
        PexCommandFlags flags = flags("--world", "ignored", "--server", "proxy-a");

        assertEquals("proxy-a", ModernCommandSupport.storageRealm(ctx, "console", flags));
    }

    @Test
    void gameServerPrefersWorldFlag() {
        CoreCloudCommandContext<String> ctx = context(CoreCloudPlatform.GAME_SERVER, "survival");
        PexCommandFlags flags = flags("--server", "ignored", "--world", "survival");

        assertEquals("survival", ModernCommandSupport.storageRealm(ctx, "console", flags));
    }

    @Test
    void fallsBackToSenderDefaultWorld() {
        CoreCloudCommandContext<String> ctx = context(CoreCloudPlatform.GAME_SERVER, "creative");

        assertEquals("creative", ModernCommandSupport.storageRealm(ctx, "console", PexCommandFlags.EMPTY));
        assertEquals("global", ModernCommandSupport.displayRealm(null));
    }

    private static CoreCloudCommandContext<String> context(CoreCloudPlatform platform, String defaultWorld) {
        return new CoreCloudCommandContext<>(
                null,
                String.class,
                new CoreCommandService(null),
                new CoreCloudCommandRegistrar.SenderAdapter<>() {
                    @Override
                    public void reply(String sender, String message) {}

                    @Override
                    public String defaultWorld(String sender) {
                        return defaultWorld;
                    }

                    @Override
                    public ru.tehkode.permissions.PermissionUser actor(String sender) {
                        return null;
                    }

                    @Override
                    public String helpText() {
                        return "";
                    }

                    @Override
                    public String pluginVersion() {
                        return "";
                    }
                },
                () -> {},
                new CoreCommandService.ConfigBridge() {
                    @Override
                    public Object get(String path) {
                        return null;
                    }

                    @Override
                    public void set(String path, Object value) {}

                    @Override
                    public void save() {}
                },
                force -> "",
                null,
                platform,
                CommandFramework.MODERN);
    }

    private static PexCommandFlags flags(String... parts) {
        java.util.ArrayDeque<String> input = new java.util.ArrayDeque<>();
        for (String part : parts) {
            input.add(part);
        }
        return PexCommandFlags.parseOptional(input);
    }
}
