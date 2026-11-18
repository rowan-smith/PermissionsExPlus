package dev.rono.permissions.bukkit17.vault;

import lombok.var;

import dev.rono.permissions.api.PexApi;
import dev.rono.permissions.bukkit17.Bukkit17Plugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.ServicePriority;

import java.util.logging.Level;

public class VaultHookManager {
    private final Bukkit17Plugin plugin;
    private final PexApi api;

    private PermissionsExPlusVaultChat chat = null;
    private PermissionsExPlusVaultPermission permission = null;

    public VaultHookManager(Bukkit17Plugin plugin, PexApi api) {
        this.plugin = plugin;
        this.api = api;
    }

    public void hook() {
        try {
            if (permission == null) {
                permission = new PermissionsExPlusVaultPermission(plugin, api);
            }

            if (chat == null) {
                chat = new PermissionsExPlusVaultChat(plugin, permission, api);
            }

            var servicesManager = plugin.getServer().getServicesManager();
            servicesManager.register(Permission.class, permission, plugin, ServicePriority.High);
            servicesManager.register(Chat.class, chat, plugin, ServicePriority.High);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred attempting to hook Vault.", e);
        }
    }

    public void unhook() {
        var servicesManager = plugin.getServer().getServicesManager();

        if (permission != null) {
            servicesManager.unregister(Permission.class, permission);
            permission = null;
        }

        if (chat != null) {
            servicesManager.unregister(Chat.class, chat);
            chat = null;
        }
    }
}
