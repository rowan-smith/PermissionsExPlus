package ru.tehkode.permissions.bukkit.regexperms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

/**
 * ABI shell retained for classic PEX imports. Injection is a no-op.
 */
public class PermissionList extends HashMap<String, Permission> {
    public PermissionList() {}

    public PermissionList(Map<? extends String, ? extends Permission> existing) {
        super(existing);
    }

    public static PermissionList inject(PluginManager manager) {
        return new PermissionList();
    }

    @Override
    public Permission put(String k, Permission v) {
        return super.put(k, v);
    }

    @Override
    public Permission remove(Object k) {
        return super.remove(k);
    }

    @Override
    public void clear() {
        super.clear();
    }

    public Collection<Map.Entry<String, Boolean>> getParents(String permission) {
        return Collections.emptyList();
    }

    /** Retained nested type for binary compatibility with classic PEX. */
    public static class NotifyingChildrenMap extends LinkedHashMap<String, Boolean> {
        public NotifyingChildrenMap(Permission perm) {}

        @Override
        public Boolean remove(Object perm) {
            return super.remove(perm);
        }

        @Override
        public Boolean put(String perm, Boolean val) {
            return super.put(perm, val);
        }

        @Override
        public void clear() {
            super.clear();
        }
    }
}
