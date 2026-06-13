package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.subject.PermissionSubject;
import dev.rono.permissions.api.subject.SubjectType;
import dev.rono.permissions.api.subject.TimedPermissionEntry;
import dev.rono.permissions.api.world.Worlds;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.api.ModernWorlds;
import ru.tehkode.permissions.PermissionEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class AbstractPermissionSubjectAdapter implements PermissionSubject {

    protected final PermissionEntity delegate;
    protected final DefaultPermissionManager manager;

    AbstractPermissionSubjectAdapter(PermissionEntity delegate, DefaultPermissionManager manager) {
        this.delegate = delegate;
        this.manager = manager;
    }

    @Override
    public abstract SubjectType type();

    @Override
    public String identifier() {
        return delegate.getIdentifier();
    }

    @Override
    public String name() {
        return delegate.getName();
    }

    @Override
    public boolean virtual() {
        return delegate.isVirtual();
    }

    @Override
    public boolean has(String permission, String world) {
        return delegate.has(permission, ModernWorlds.toLegacy(world));
    }

    @Override
    public List<String> permissions(String world) {
        return delegate.getOwnPermissions(ModernWorlds.toLegacy(world));
    }

    @Override
    public List<String> effectivePermissions(String world) {
        return delegate.getPermissions(ModernWorlds.toLegacy(world));
    }

    @Override
    public void addPermission(String permission, String world) {
        delegate.addPermission(permission, ModernWorlds.toLegacy(world));
    }

    @Override
    public void removePermission(String permission, String world) {
        delegate.removePermission(permission, ModernWorlds.toLegacy(world));
    }

    @Override
    public void setPermissions(List<String> permissions, String world) {
        delegate.setPermissions(permissions, ModernWorlds.toLegacy(world));
    }

    @Override
    public void addTimedPermission(String permission, String world, int lifetimeSeconds) {
        delegate.addTimedPermission(permission, ModernWorlds.toLegacy(world), lifetimeSeconds);
    }

    @Override
    public void removeTimedPermission(String permission, String world) {
        delegate.removeTimedPermission(permission, ModernWorlds.toLegacy(world));
    }

    @Override
    public List<String> timedPermissions(String world) {
        return delegate.getTimedPermissions(ModernWorlds.toLegacy(world));
    }

    @Override
    public String prefix(String world) {
        return delegate.getPrefix(ModernWorlds.toLegacy(world));
    }

    @Override
    public String suffix(String world) {
        return delegate.getSuffix(ModernWorlds.toLegacy(world));
    }

    @Override
    public void setPrefix(String prefix, String world) {
        delegate.setPrefix(prefix, ModernWorlds.toLegacy(world));
    }

    @Override
    public void setSuffix(String suffix, String world) {
        delegate.setSuffix(suffix, ModernWorlds.toLegacy(world));
    }

    @Override
    public String option(String key, String world) {
        return delegate.getOption(key, ModernWorlds.toLegacy(world), null);
    }

    @Override
    public void setOption(String key, String value, String world) {
        delegate.setOption(key, value, ModernWorlds.toLegacy(world));
    }

    @Override
    public Map<String, String> options(String world) {
        return delegate.getOptions(ModernWorlds.toLegacy(world));
    }

    @Override
    public Set<String> configuredWorlds() {
        var worlds = new LinkedHashSet<String>();
        for (String world : delegate.getWorlds()) {
            worlds.add(Worlds.fromMapKey(world));
        }
        return Set.copyOf(worlds);
    }

    @Override
    public Map<String, List<String>> permissionsByWorld() {
        var mapped = new LinkedHashMap<String, List<String>>();
        for (Map.Entry<String, List<String>> entry : delegate.getAllPermissions().entrySet()) {
            mapped.put(Worlds.fromMapKey(entry.getKey()), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(mapped);
    }

    @Override
    public Map<String, List<String>> effectivePermissionsByWorld() {
        var worlds = new LinkedHashSet<String>();
        worlds.add(Worlds.GLOBAL);
        worlds.addAll(configuredWorlds());
        var mapped = new LinkedHashMap<String, List<String>>();
        for (String world : worlds) {
            mapped.put(world, effectivePermissions(world));
        }
        return Map.copyOf(mapped);
    }

    @Override
    public List<TimedPermissionEntry> timedPermissionEntries(String world) {
        var legacyWorld = ModernWorlds.toLegacy(world);
        var apiWorld = Worlds.normalize(world);
        var entries = new ArrayList<TimedPermissionEntry>();
        for (String permission : delegate.getTimedPermissions(legacyWorld)) {
            entries.add(new TimedPermissionEntry(
                    permission, apiWorld, delegate.getTimedPermissionLifetime(permission, legacyWorld)));
        }
        return List.copyOf(entries);
    }

    @Override
    public int timedPermissionRemainingSeconds(String permission, String world) {
        return delegate.getTimedPermissionLifetime(permission, ModernWorlds.toLegacy(world));
    }

    @Override
    public void save() {
        delegate.save();
    }
}
