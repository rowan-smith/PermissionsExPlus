package ru.tehkode.permissions.bukkit.regexperms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.PluginManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * ABI shell. Subscription-map injection is owned by PermissionsExPlus.
 */
public class PEXPermissionSubscriptionMap extends HashMap<String, Map<Permissible, Boolean>> {
    public static PEXPermissionSubscriptionMap inject(PermissionsEx plugin, PluginManager manager) {
        return new PEXPermissionSubscriptionMap();
    }

    public void uninject() {}

    @Override
    public Map<Permissible, Boolean> get(Object key) {
        Map<Permissible, Boolean> existing = super.get(key);
        return existing == null ? Collections.emptyMap() : existing;
    }

    @Override
    public Map<Permissible, Boolean> put(String key, Map<Permissible, Boolean> value) {
        return super.put(key, value);
    }

    public class PEXSubscriptionValueMap implements Map<Permissible, Boolean> {
        private final Map<Permissible, Boolean> backing;

        public PEXSubscriptionValueMap(String permission, Map<Permissible, Boolean> backing) {
            this.backing = backing == null ? new HashMap<>() : backing;
        }

        @Override
        public int size() {
            return backing.size();
        }

        @Override
        public boolean isEmpty() {
            return backing.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return backing.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return backing.containsValue(value);
        }

        @Override
        public Boolean put(Permissible key, Boolean value) {
            return backing.put(key, value);
        }

        @Override
        public Boolean remove(Object key) {
            return backing.remove(key);
        }

        @Override
        public void putAll(Map<? extends Permissible, ? extends Boolean> m) {
            backing.putAll(m);
        }

        @Override
        public void clear() {
            backing.clear();
        }

        @Override
        public Boolean get(Object key) {
            return backing.get(key);
        }

        @Override
        public Set<Permissible> keySet() {
            return backing.keySet();
        }

        @Override
        public Collection<Boolean> values() {
            return backing.values();
        }

        @Override
        public Set<Entry<Permissible, Boolean>> entrySet() {
            return backing.entrySet();
        }
    }
}
