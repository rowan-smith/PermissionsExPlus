package dev.rono.permissions.paper;

import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import org.bukkit.command.CommandSender;
import ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin;
import ru.tehkode.permissions.spigot.bukkit.regexperms.PermissionList;
import ru.tehkode.permissions.spigot.bukkit.regexperms.RegexPermissions;

import java.util.function.Function;

/**
 * Paper runtime entry point. Uses {@link cloud.commandframework.paper.PaperCommandManager} for
 * Paper-native Brigadier integration.
 */
public class PaperPermissionsExPlugin extends SpigotPermissionsExPlugin {
    private PaperPermissionBridge paperPermissionBridge;

    @Override
    protected BukkitCommandManager<CommandSender> createCloudCommandManager() throws Exception {
        return new StrippingPaperCommandManager<>(
                this,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity());
    }

    @Override
    protected void configureCloudBrigadier(BukkitCommandManager<CommandSender> manager) {
        if (!(manager instanceof PaperCommandManager<?> paperManager)) {
            super.configureCloudBrigadier(manager);
            return;
        }
        try {
            paperManager.registerBrigadier();
        } catch (BukkitCommandManager.BrigadierFailureException ex) {
            getLogger().fine("Paper Brigadier hook not available: " + ex.getMessage());
        }
    }

    @Override
    public boolean installRegexPermissionSubscription(RegexPermissions regexPermissions, PermissionList permsList) {
        paperPermissionBridge = new PaperPermissionBridge(this, permsList);
        if (paperPermissionBridge.install()) {
            return true;
        }
        paperPermissionBridge = null;
        return false;
    }

    @Override
    public void uninstallRegexPermissionSubscription(RegexPermissions regexPermissions) {
        if (paperPermissionBridge != null) {
            paperPermissionBridge.uninstall();
            paperPermissionBridge = null;
        }
    }
}
