package dev.rono.permissions.core.commands;

import cloud.commandframework.CommandManager;
import ru.tehkode.permissions.PermissionUser;

public final class CoreCloudCommandRegistrar<C> {
    private final CoreCloudCommandContext<C> context;

    public CoreCloudCommandRegistrar(
            CommandManager<C> manager,
            Class<C> senderType,
            CoreCommandService commandService,
            SenderAdapter<C> senderAdapter,
            CoreCommandService.CoreConfigReloader configReloader,
            CoreCommandService.ConfigBridge configBridge,
            CoreCommandService.UuidConversionBridge uuidConversionBridge) {
        this(
                manager,
                senderType,
                commandService,
                senderAdapter,
                configReloader,
                configBridge,
                uuidConversionBridge,
                CoreCloudPlatform.GAME_SERVER);
    }

    public CoreCloudCommandRegistrar(
            CommandManager<C> manager,
            Class<C> senderType,
            CoreCommandService commandService,
            SenderAdapter<C> senderAdapter,
            CoreCommandService.CoreConfigReloader configReloader,
            CoreCommandService.ConfigBridge configBridge,
            CoreCommandService.UuidConversionBridge uuidConversionBridge,
            CoreCloudPlatform cloudPlatform) {
        this(
                manager,
                senderType,
                commandService,
                senderAdapter,
                configReloader,
                configBridge,
                uuidConversionBridge,
                null,
                cloudPlatform);
    }

    public CoreCloudCommandRegistrar(
            CommandManager<C> manager,
            Class<C> senderType,
            CoreCommandService commandService,
            SenderAdapter<C> senderAdapter,
            CoreCommandService.CoreConfigReloader configReloader,
            CoreCommandService.ConfigBridge configBridge,
            CoreCommandService.UuidConversionBridge uuidConversionBridge,
            CoreCommandService.ImportBridge importBridge,
            CoreCloudPlatform cloudPlatform) {
        this.context = new CoreCloudCommandContext<>(
                manager,
                senderType,
                commandService,
                senderAdapter,
                configReloader,
                configBridge,
                uuidConversionBridge,
                importBridge,
                cloudPlatform);
    }

    public void register() {
        CoreCloudAnnotationCommands.register(context);
    }


    public interface SenderAdapter<C> {
        void reply(C sender, String message);
        String defaultWorld(C sender);
        PermissionUser actor(C sender);
        String helpText();
        String pluginVersion();
        default String reportText() { return "Fill in the issue information at https://github.com/PEXPlugins/PermissionsEx/issues"; }
        default String superPermsText(C sender, String user) { return "Superperms listing is not available on this platform."; }
    }
}
