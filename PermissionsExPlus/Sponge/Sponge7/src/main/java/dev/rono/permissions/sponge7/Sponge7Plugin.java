package dev.rono.permissions.sponge7;

import com.google.inject.Inject;
import dev.rono.permissions.api.PexApi;
import dev.rono.permissions.api.PexRegistration;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.sponge7.platform.Sponge7Platform;
import dev.rono.permissions.sponge7.service.PexPermissionService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.world.World;

@Plugin(id = "permissionsexplus", name = "PermissionsExPlus", version = Sponge7Plugin.VERSION, description = "PermissionsExPlus permission service for Sponge", authors = {"Rono"})
public final class Sponge7Plugin {
    /** Kept in step with the Maven project version. */
    static final String VERSION = "2.0.0-SNAPSHOT";

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDirectory;

    private PexApiImpl<CommandSource> core;

    @Listener(order = Order.FIRST)
    public void onPreInitialization(GamePreInitializationEvent event) {
        core = new PexApiImpl<>(new Sponge7Platform(this, logger, configDirectory));
        core.start();

        // Registered early so plugins that look up the permission service find this
        // one.
        Sponge.getServiceManager().setProvider(this, PermissionService.class, new PexPermissionService(core));
        Sponge.getServiceManager().setProvider(this, PexApi.class, core);

        core.contexts().registry().registerContextType("world", Sponge7Plugin::worldNames);

        PexRegistration.register(core);
    }

    private static List<String> worldNames() {
        List<String> names = new ArrayList<>();

        if (Sponge.isServerAvailable()) {
            for (World world : Sponge.getServer().getWorlds()) {
                names.add(world.getName());
            }
        }

        return names;
    }

    @Listener(order = Order.EARLY)
    public void onAuth(ClientConnectionEvent.Auth event) {
        if (core == null || !core.config().preloadOnJoin()) {
            return;
        }

        try {
            core.users().loadOrCreateUser(event.getProfile().getUniqueId(), event.getProfile().getName().orElse(event.getProfile().getUniqueId().toString()))
                    .toCompletableFuture().join();
        } catch (RuntimeException ignored) {
            // Loaded lazily on first permission check instead.
        }
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        UUID id = player.getUniqueId();

        core.users().cache().markOnline(id);
        core.stateTracker().updateState(id, "world", player.getWorld().getName());
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        UUID id = event.getTargetEntity().getUniqueId();

        core.users().cache().markOffline(id);
        core.stateTracker().clearState(id);
    }

    @Listener
    public void onStopping(GameStoppingServerEvent event) {
        if (core == null) {
            return;
        }

        PexRegistration.unregister(core);
        core.stop();
    }
}
