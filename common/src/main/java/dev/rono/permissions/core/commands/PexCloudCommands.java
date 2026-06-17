package dev.rono.permissions.core.commands;

import cloud.commandframework.CommandManager;
import dev.rono.permissions.core.config.CommandFramework;
import ru.tehkode.permissions.PermissionManager;

/**
 * One-call registration of the shared PermissionsEx Cloud command tree on any platform.
 */
public final class PexCloudCommands {
    private PexCloudCommands() {}

    /**
     * Registers all core {@code /pex} commands and returns the command service backing them.
     */
    public static <C> CoreCommandService install(InstallRequest<C> request) {
        CoreCommandService commandService = new CoreCommandService(request.permissionManager());
        CoreCloudCommandRegistrar<C> registrar = request.importBridge() == null
                ? new CoreCloudCommandRegistrar<>(
                        request.manager(),
                        request.senderType(),
                        commandService,
                        request.senderAdapter(),
                        request.configReloader(),
                        request.configBridge(),
                        request.uuidConversionBridge(),
                        request.platform(),
                        request.commandFramework())
                : new CoreCloudCommandRegistrar<>(
                        request.manager(),
                        request.senderType(),
                        commandService,
                        request.senderAdapter(),
                        request.configReloader(),
                        request.configBridge(),
                        request.uuidConversionBridge(),
                        request.platform(),
                        request.commandFramework(),
                        request.importBridge());
        registrar.register();
        return commandService;
    }

    public record InstallRequest<C>(
            CommandManager<C> manager,
            Class<C> senderType,
            PermissionManager permissionManager,
            CoreCloudCommandRegistrar.SenderAdapter<C> senderAdapter,
            CoreCommandService.CoreConfigReloader configReloader,
            CoreCommandService.ConfigBridge configBridge,
            CoreCommandService.UuidConversionBridge uuidConversionBridge,
            CoreCloudPlatform platform,
            CommandFramework commandFramework,
            CoreCommandService.ImportBridge importBridge) {

        public InstallRequest(
                CommandManager<C> manager,
                Class<C> senderType,
                PermissionManager permissionManager,
                CoreCloudCommandRegistrar.SenderAdapter<C> senderAdapter,
                CoreCommandService.CoreConfigReloader configReloader,
                CoreCommandService.ConfigBridge configBridge,
                CoreCommandService.UuidConversionBridge uuidConversionBridge,
                CoreCloudPlatform platform) {
            this(
                    manager,
                    senderType,
                    permissionManager,
                    senderAdapter,
                    configReloader,
                    configBridge,
                    uuidConversionBridge,
                    platform,
                    CommandFramework.MODERN,
                    null);
        }

        public InstallRequest(
                CommandManager<C> manager,
                Class<C> senderType,
                PermissionManager permissionManager,
                CoreCloudCommandRegistrar.SenderAdapter<C> senderAdapter,
                CoreCommandService.CoreConfigReloader configReloader,
                CoreCommandService.ConfigBridge configBridge,
                CoreCommandService.UuidConversionBridge uuidConversionBridge,
                CoreCloudPlatform platform,
                CommandFramework commandFramework) {
            this(
                    manager,
                    senderType,
                    permissionManager,
                    senderAdapter,
                    configReloader,
                    configBridge,
                    uuidConversionBridge,
                    platform,
                    commandFramework,
                    null);
        }

        public InstallRequest<C> withImportBridge(CoreCommandService.ImportBridge importBridge) {
            return new InstallRequest<>(
                    manager,
                    senderType,
                    permissionManager,
                    senderAdapter,
                    configReloader,
                    configBridge,
                    uuidConversionBridge,
                    platform,
                    commandFramework,
                    importBridge);
        }
    }
}
