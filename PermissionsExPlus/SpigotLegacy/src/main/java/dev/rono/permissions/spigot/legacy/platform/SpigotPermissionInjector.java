package dev.rono.permissions.spigot.legacy.platform;

import lombok.var;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.permission.PermissionResult;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.plugin.Plugin;
import dev.rono.permissions.core.util.Immutables;

/**
 * Injector for the CraftBukkit player permissible. The declaring class is
 * discovered from the live player so both versioned (1.13-1.20.4) and modern
 * unversioned CraftBukkit packages are supported.
 */
public final class SpigotPermissionInjector implements Listener, AutoCloseable {
    private static final String PERMISSIBLE_FIELD = "perm";

    private final Plugin plugin;

    private final BiFunction<Player, String, PermissionResult> resolver;
    private final Function<Player, Map<String, PermissionResult>> effectivePermissions;
    private final Map<UUID, Injection> injections = new HashMap<>();

    private Field field;
    private boolean unavailableReported;

    private volatile PermissionHookStatus status = PermissionHookStatus.PENDING;

    /**
     * Whether PermissionsExPlus controls native {@code Player#hasPermission}
     * checks on this server.
     */
    public enum PermissionHookStatus {
        /** No player has been injected yet. */
        PENDING,
        /** The player permissible is replaced; native checks use PermissionsExPlus. */
        ACTIVE,
        /** The server does not expose the permissible integration point. */
        UNAVAILABLE,
        /** Another permissions provider already owns the player permissible. */
        CONFLICTED
    }

    public PermissionHookStatus status() {
        return status;
    }

    public SpigotPermissionInjector(Plugin plugin, BiFunction<Player, String, PermissionResult> resolver) {
        this(plugin, resolver, player -> Immutables.mapOf());
    }

    public SpigotPermissionInjector(Plugin plugin, BiFunction<Player, String, PermissionResult> resolver, Function<Player, Map<String, PermissionResult>> effectivePermissions) {
        this.plugin = plugin;
        this.resolver = resolver;
        this.effectivePermissions = effectivePermissions;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        inject(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        uninject(event.getPlayer());
    }

    public boolean inject(Player player) {
        if (injections.containsKey(player.getUniqueId())) {
            return true;
        }

        var target = targetField(player);
        if (target == null) {
            return false;
        }

        try {
            var original = (Permissible) target.get(player);
            if (!(original instanceof PermissibleBase)) {
                reportUnavailable(PermissionHookStatus.CONFLICTED, "another permissions provider already owns CraftHumanEntity#perm (" + original.getClass().getName() + ")");
                return false;
            }

            var replacement = new PermissionsExPlusPermissible(player, original, permission -> resolver.apply(player, permission), () -> effectivePermissions.apply(player));

            target.set(player, replacement);

            injections.put(player.getUniqueId(), new Injection(player, original, replacement));

            if (status == PermissionHookStatus.PENDING) {
                status = PermissionHookStatus.ACTIVE;
            }

            return true;
        } catch (IllegalAccessException exception) {
            reportUnavailable(PermissionHookStatus.UNAVAILABLE, "cannot access CraftHumanEntity#perm: " + exception.getMessage());

            return false;
        }
    }

    /**
     * Recalculates Bukkit attachments after PermissionsExPlus data or context
     * changes.
     */
    public void refresh(Player player) {
        var injection = injections.get(player.getUniqueId());
        if (injection != null) {
            injection.replacement.recalculatePermissions();

            LegacyCompat.updateCommands(player);
        }
    }

    public void refreshAll() {
        Immutables.copyOfList(injections.values()).forEach(injection -> refresh(injection.player));
    }

    public void uninject(Player player) {
        var injection = injections.remove(player.getUniqueId());
        if (injection == null || field == null) {
            return;
        }

        try {
            if (field.get(player) == injection.replacement) {
                field.set(player, injection.original);
            }
        } catch (IllegalAccessException exception) {
            plugin.getLogger().warning(
                    "Could not restore Bukkit permissible for " + player.getName() + ": " + exception.getMessage());
        }
    }

    @Override
    public void close() {
        Immutables.copyOfList(injections.values()).forEach(injection -> uninject(injection.player));
    }

    private Field targetField(Player player) {
        if (field != null) {
            return field;
        }

        try {
            Field candidate = null;
            for (Class<?> type = player.getClass(); type != null; type = type.getSuperclass()) {
                try {
                    candidate = type.getDeclaredField(PERMISSIBLE_FIELD);
                    break;
                } catch (NoSuchFieldException ignored) {
                    // CraftHumanEntity may live in a versioned CraftBukkit package.
                }
            }

            if (candidate == null || !Permissible.class.isAssignableFrom(candidate.getType()) || !makeAccessible(candidate)) {
                reportUnavailable(PermissionHookStatus.UNAVAILABLE, "CraftHumanEntity#perm is not an accessible Permissible");
                return null;
            }

            return field = candidate;
        } catch (LinkageError | RuntimeException exception) {
            reportUnavailable(PermissionHookStatus.UNAVAILABLE, "this server does not expose the supported CraftHumanEntity#perm integration point");
            return null;
        }
    }

    /**
     * {@code Field#trySetAccessible()} is Java 9; this build also runs on Java 8.
     */
    private static boolean makeAccessible(Field field) {
        try {
            field.setAccessible(true);
            return true;
        } catch (RuntimeException denied) {
            return false;
        }
    }

    private void reportUnavailable(PermissionHookStatus failure, String reason) {
        status = failure;

        if (!unavailableReported) {
            unavailableReported = true;

            // Vault is not a substitute: plugins calling Player#hasPermission directly
            // bypass it. Make the loss of native enforcement impossible to miss.
            plugin.getLogger().severe("Native permission checks are NOT controlled by PermissionsExPlus: " + reason
                    + ". Plugins that call Player#hasPermission directly will ignore PermissionsExPlus groups and"
                    + " permissions; Vault only covers plugins that use the Vault API. Remove the conflicting"
                    + " provider or use a supported server build.");
        }
    }

    @Value
    @Accessors(fluent = true)
    private static class Injection {
        Player player;
        Permissible original;
        Permissible replacement;

        private Injection(Player player, Permissible original, Permissible replacement) {
            this.player = player;
            this.original = original;
            this.replacement = replacement;
        }
    }
}
