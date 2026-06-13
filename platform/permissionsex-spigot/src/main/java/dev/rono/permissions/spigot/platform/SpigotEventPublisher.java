package dev.rono.permissions.spigot.platform;

import dev.rono.permissions.api.bus.EntityDispatch;
import dev.rono.permissions.api.bus.PermissionDispatch;
import dev.rono.permissions.api.bus.SystemDispatch;
import dev.rono.permissions.api.runtime.PlatformEventBus;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionEvent;
import ru.tehkode.permissions.events.PermissionSystemEvent;

/**
 * Translates core bus dispatches into legacy Bukkit {@link PermissionEvent} instances.
 */
public final class SpigotEventPublisher implements PlatformEventBus {
    private final JavaPlugin plugin;
    private final java.util.function.Supplier<PermissionManager> managerSupplier;

    public SpigotEventPublisher(JavaPlugin plugin, PermissionManager manager) {
        this(plugin, () -> manager);
    }

    public SpigotEventPublisher(JavaPlugin plugin, java.util.function.Supplier<PermissionManager> managerSupplier) {
        this.plugin = plugin;
        this.managerSupplier = managerSupplier;
    }

    public void callEvent(PermissionEvent event) {
        plugin.getServer().getPluginManager().callEvent(event);
    }

    @Override
    public void publish(PermissionDispatch dispatch) {
        if (dispatch instanceof EntityDispatch ed) {
            PermissionEntity entity = resolveEntity(ed.entityIdentifier(), ed.entityType());
            callEvent(new PermissionEntityEvent(
                    ed.sourceId(),
                    entity,
                    PermissionEntityEvent.Action.valueOf(ed.mutation().name())));
            return;
        }
        if (dispatch instanceof SystemDispatch sd) {
            callEvent(new PermissionSystemEvent(
                    sd.sourceId(),
                    PermissionSystemEvent.Action.valueOf(sd.mutation().name())));
            return;
        }
        throw new IllegalArgumentException("Unknown dispatch: " + dispatch.getClass().getName());
    }

    private PermissionEntity resolveEntity(String identifier, String entityType) {
        PermissionManager manager = managerSupplier.get();
        if ("GROUP".equalsIgnoreCase(entityType)) {
            return manager.getGroup(identifier);
        }
        return manager.getUser(identifier);
    }
}
