package dev.rono.permissions.spigot.platform;

import dev.rono.permissions.api.bus.EntityDispatch;
import dev.rono.permissions.api.bus.PermissionDispatch;
import dev.rono.permissions.api.bus.SystemDispatch;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionEvent;
import ru.tehkode.permissions.events.PermissionSystemEvent;

/**
 * Translates core bus dispatches into legacy Bukkit {@link PermissionEvent} instances.
 */
public final class SpigotEventPublisher {
    private final JavaPlugin plugin;

    public SpigotEventPublisher(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void callEvent(PermissionEvent event) {
        plugin.getServer().getPluginManager().callEvent(event);
    }

    public void publish(PermissionDispatch dispatch) {
        if (dispatch instanceof EntityDispatch ed) {
            PermissionManager manager = plugin.getServer().getServicesManager()
                    .getRegistration(PermissionManager.class)
                    .getProvider();
            PermissionEntity entity = resolveEntity(manager, ed.entityIdentifier(), ed.entityType());
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

    private static PermissionEntity resolveEntity(PermissionManager manager, String identifier, String entityType) {
        if ("GROUP".equalsIgnoreCase(entityType)) {
            return manager.getGroup(identifier);
        }
        return manager.getUser(identifier);
    }
}
