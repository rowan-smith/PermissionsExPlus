package dev.rono.permissions.bukkit.platform;

import lombok.var;

import dev.rono.permissions.api.permission.PermissionResult;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

/**
 * Bukkit-compatible permissible which keeps the server's existing attachment
 * state while making PermissionsExPlus the authoritative dynamic resolver.
 */
public final class PermissionsExPlusPermissible extends PermissibleBase {
    private final Player player;
    private final Permissible delegate;

    private final Function<String, PermissionResult> resolver;
    private final Supplier<Map<String, PermissionResult>> effectivePermissions;

    /*
     * Snapshot used only to enumerate effective permissions
     * (getEffectivePermissions).
     * Authorisation decisions never read it: Core's revision- and expiry-aware
     * cache
     * is authoritative, so a timed permission stops applying the moment it expires.
     */
    private final Map<String, PermissionResult> resolved = new ConcurrentHashMap<>();

    public PermissionsExPlusPermissible(Player player,
            Permissible delegate,
            Function<String, PermissionResult> resolver,
            Supplier<Map<String, PermissionResult>> effectivePermissions) {
        super(player);

        this.player = Objects.requireNonNull(player, "player");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.effectivePermissions = Objects.requireNonNull(effectivePermissions, "effectivePermissions");

        recalculatePermissions();
    }

    private PermissionResult resolve(String permission) {
        Objects.requireNonNull(permission, "permission");
        var result = resolver.apply(permission);

        // Record the fresh decision for enumeration only; it is never read back here.
        resolved.put(permission, result);

        return result;
    }

    @Override
    public boolean hasPermission(@NonNull String permission) {
        switch (resolve(permission)) {
            case ALLOW :
                return true;
            case DENY :
                return false;
            case UNDEFINED :
                return delegate.hasPermission(permission);
            default :
                throw new IllegalStateException("Unexpected value: " + resolve(permission));
        }
    }

    @Override
    public boolean hasPermission(@NonNull Permission permission) {
        Objects.requireNonNull(permission, "permission");

        switch (resolve(permission.getName())) {
            case ALLOW :
                return true;
            case DENY :
                return false;
            case UNDEFINED :
                return delegate.hasPermission(permission);
            default :
                throw new IllegalStateException("Unexpected value: " + resolve(permission.getName()));
        }
    }

    @Override
    public boolean isPermissionSet(@NonNull String permission) {
        return resolve(permission) != PermissionResult.UNDEFINED || delegate.isPermissionSet(permission);
    }

    @Override
    public boolean isPermissionSet(@NonNull Permission permission) {
        Objects.requireNonNull(permission, "permission");
        return resolve(permission.getName()) != PermissionResult.UNDEFINED || delegate.isPermissionSet(permission);
    }

    @Override
    public @NonNull PermissionAttachment addAttachment(@NonNull Plugin plugin, @NonNull String name, boolean value) {
        return delegate.addAttachment(plugin, name, value);
    }

    @Override
    public @NonNull PermissionAttachment addAttachment(@NonNull Plugin plugin) {
        return delegate.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(@NonNull Plugin plugin, @NonNull String name, boolean value, int ticks) {
        return delegate.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(@NonNull Plugin plugin, int ticks) {
        return delegate.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(@NonNull PermissionAttachment attachment) {
        delegate.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        if (delegate == null) {
            return;
        }

        delegate.recalculatePermissions();

        resolved.clear();
        resolved.putAll(effectivePermissions.get());

        for (var permission : player.getServer().getPluginManager().getPermissions()) {
            var result = resolver.apply(permission.getName());

            if (result != PermissionResult.UNDEFINED) {
                resolved.put(permission.getName(), result);
            }
        }
    }

    @Override
    public @NonNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        var effective = new LinkedHashMap<String, PermissionAttachmentInfo>();

        for (var permission : delegate.getEffectivePermissions()) {
            effective.put(permission.getPermission().toLowerCase(Locale.ROOT), permission);
        }

        resolved.forEach((permission, result) -> {
            if (result != PermissionResult.UNDEFINED) {
                effective.put(permission.toLowerCase(Locale.ROOT), new PermissionAttachmentInfo(this, permission, null, result == PermissionResult.ALLOW));
            }
        });

        return Collections.unmodifiableSet(new LinkedHashSet<>(effective.values()));
    }

    @Override
    public boolean isOp() {
        return delegate.isOp();
    }

    @Override
    public void setOp(boolean value) {
        delegate.setOp(value);
    }
}
