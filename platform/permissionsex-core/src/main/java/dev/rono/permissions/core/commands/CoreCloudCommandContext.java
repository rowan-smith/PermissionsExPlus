package dev.rono.permissions.core.commands;

import cloud.commandframework.CommandManager;

/**
 * Shared Cloud registration wiring (accessible from {@code dev.rono.permissions.core.commands.cloud}).
 */
public record CoreCloudCommandContext<C>(
        CommandManager<C> manager,
        Class<C> senderType,
        CoreCommandService commandService,
        CoreCloudCommandRegistrar.SenderAdapter<C> senderAdapter,
        CoreCommandService.CoreConfigReloader configReloader,
        CoreCommandService.ConfigBridge configBridge,
        CoreCommandService.UuidConversionBridge uuidConversionBridge,
        CoreCommandService.ImportBridge importBridge,
        CoreCloudPlatform cloudPlatform) {
    /** {@link CoreCloudPlatform#GAME_SERVER} */
    public CoreCloudCommandContext(
            CommandManager<C> manager,
            Class<C> senderType,
            CoreCommandService commandService,
            CoreCloudCommandRegistrar.SenderAdapter<C> senderAdapter,
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
                null,
                CoreCloudPlatform.GAME_SERVER);
    }
}
