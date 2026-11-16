package dev.rono.permissions.spigot.legacy;

import lombok.var;

import dev.rono.permissions.api.PexApi;
import dev.rono.permissions.api.PexRegistration;
import dev.rono.permissions.api.event.Subscription;
import dev.rono.permissions.api.event.group.GroupCreatedEvent;
import dev.rono.permissions.api.event.group.GroupDeletedEvent;
import dev.rono.permissions.api.event.group.GroupModifiedEvent;
import dev.rono.permissions.api.event.user.UserModifiedEvent;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.spigot.legacy.listener.PlayerListener;
import dev.rono.permissions.spigot.legacy.listener.SpigotStateListener;
import dev.rono.permissions.spigot.legacy.placeholder.PlaceholderApiHookManager;
import dev.rono.permissions.spigot.legacy.platform.SpigotPermissionInjector;
import dev.rono.permissions.spigot.legacy.platform.SpigotPlatform;
import dev.rono.permissions.spigot.legacy.platform.LegacyCompat;
import dev.rono.permissions.spigot.legacy.vault.VaultHookManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import dev.rono.permissions.core.util.Immutables;

public class SpigotLegacyPlugin extends JavaPlugin {
    private PexApiImpl<CommandSender> core;

    private SpigotPermissionInjector permissionInjector;
    private final List<Subscription> permissionRefreshSubscriptions = new ArrayList<>();

    private VaultHookManager vaultHookManager = null;
    private PlaceholderApiHookManager placeholderApiHookManager = null;

    @Override
    public void onEnable() {
        var platform = new SpigotPlatform(this);
        core = new PexApiImpl<>(platform);

        try {
            core.start();

            completeEnable();
        } catch (RuntimeException error) {
            failEnable(error);
        }
    }

    private void completeEnable() {

        core.contexts().registry().registerContextType("world", () -> getServer().getWorlds().stream().map(World::getName).collect(Immutables.toList()));
        core.contexts().registry().registerContextType("gamemode", () -> Arrays.stream(GameMode.values()).map(gameMode -> gameMode.name().toLowerCase(Locale.ROOT)).collect(Immutables.toList()));

        permissionInjector = new SpigotPermissionInjector(this, this::resolvePermission,
                player -> core.users().cache().get(player.getUniqueId())
                        .map(user -> core.resolvers().resolve(user, core.contexts().queryOptions(player.getUniqueId())).permissions().permissionMap())
                        .orElseGet(java.util.Collections::emptyMap));

        var stateListener = new SpigotStateListener(core.stateTracker(), this::refreshPermissions);

        getServer().getPluginManager().registerEvents(new PlayerListener(core), this);
        getServer().getPluginManager().registerEvents(permissionInjector, this);
        getServer().getPluginManager().registerEvents(stateListener, this);

        getServer().getOnlinePlayers().forEach(player -> {
            core.users().cache().markOnline(player.getUniqueId());

            if (core.config().preloadOnJoin()) {
                core.users().loadOrCreateUserAsync(player.getUniqueId(), player.getName(), user -> {});
            }

            permissionInjector.inject(player);
            stateListener.track(player);

            refreshPermissions(player);
        });

        registerPermissionRefreshes();

        PexRegistration.register(core);

        getServer().getServicesManager().register(PexApi.class, core, this, ServicePriority.Normal);

        hookVault();
        hookPlaceholderApi();
    }

    private void failEnable(Throwable error) {
        getLogger().log(Level.SEVERE, "Unable to initialize PermissionsExPlus", error);
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        if (permissionInjector != null) {
            permissionInjector.close();
        }

        permissionRefreshSubscriptions.forEach(Subscription::unsubscribe);
        permissionRefreshSubscriptions.clear();

        getServer().getServicesManager().unregister(PexApi.class, core);

        PexRegistration.unregister(core);

        unhookVault();
        unhookPlaceholderApi();

        core.stop();
    }

    private void registerPermissionRefreshes() {
        permissionRefreshSubscriptions.add(core.events().subscribe(UserModifiedEvent.class, event -> refresh(event.current().uniqueId())));
        permissionRefreshSubscriptions.add(core.events().subscribe(GroupModifiedEvent.class, event -> refreshAll()));
        permissionRefreshSubscriptions.add(core.events().subscribe(GroupCreatedEvent.class, event -> refreshAll()));
        permissionRefreshSubscriptions.add(core.events().subscribe(GroupDeletedEvent.class, event -> refreshAll()));
        // The event bus survives reload, but the data behind every injected permissible
        // was replaced, so recalculate all online players once the new runtime is live.
        permissionRefreshSubscriptions.add(core.onReload(this::refreshAll));
    }

    private void refresh(UUID uuid) {
        getServer().getScheduler().runTask(this, () -> {
            var player = getServer().getPlayer(uuid);

            if (player != null) {
                permissionInjector.refresh(player);
            }
        });
    }

    private void refreshAll() {
        getServer().getScheduler().runTask(this, permissionInjector::refreshAll);
    }

    private void hookVault() {
        if (!core.config().vaultEnabled() || getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        if (vaultHookManager != null) {
            return;
        }

        vaultHookManager = new VaultHookManager(this, core);
        vaultHookManager.hook();
    }

    private void unhookVault() {
        if (!core.config().vaultEnabled()) {
            return;
        }

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        if (vaultHookManager == null) {
            return;
        }

        vaultHookManager.unhook();
        vaultHookManager = null;
    }

    private void hookPlaceholderApi() {
        if (!core.config().placeholderApiEnabled()) {
            return;
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        if (placeholderApiHookManager != null) {
            return;
        }

        placeholderApiHookManager = new PlaceholderApiHookManager(this, core);
        placeholderApiHookManager.hook();
    }

    private void unhookPlaceholderApi() {
        if (!core.config().placeholderApiEnabled()) {
            return;
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        if (placeholderApiHookManager == null) {
            return;
        }

        placeholderApiHookManager.unhook();
        placeholderApiHookManager = null;
    }

    private void refreshPermissions(Player player) {
        permissionInjector.refresh(player);

        LegacyCompat.updateCommands(player);
    }

    private PermissionResult resolvePermission(Player player, String node) {
        var cached = core.users().cache().get(player.getUniqueId());

        if (cached.isPresent()) {
            return core.resolvers().permissions().check(cached.get(), node, core.contexts().queryOptions(player.getUniqueId()));
        }

        core.users().loadOrCreateUserAsync(player.getUniqueId(), player.getName(), user -> {
            if (player.isOnline()) {
                refreshPermissions(player);
            }
        });

        return core.cacheFailureFallback(player.getUniqueId(), node);
    }
}
