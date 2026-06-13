package dev.rono.permissions.bungee;

import ru.tehkode.permissions.PEXBackendConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class BungeeBackendConfiguration implements PEXBackendConfiguration {
    private final String name;
    private final Map<String, Object> data;

    BungeeBackendConfiguration(String name, Map<String, Object> data) {
        this.name = name;
        this.data = data;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getString(String path) {
        Object value = get(path);
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public String getString(String path, String def) {
        String value = getString(path);
        return value == null ? def : value;
    }

    @Override
    public void set(String path, Object value) {
        put(path, value);
    }

    @Override
    public List<String> getStringList(String path) {
        Object value = get(path);
        if (value instanceof List<?> list) {
            List<String> ret = new ArrayList<>(list.size());
            for (Object item : list) {
                ret.add(String.valueOf(item));
            }
            return ret;
        }
        return new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public PEXBackendConfiguration getConfigurationSection(String path) {
        Object value = get(path);
        if (value instanceof Map<?, ?> map) {
            return new BungeeBackendConfiguration(path, (Map<String, Object>) map);
        }
        return null;
    }

    @Override
    public PEXBackendConfiguration createSection(String path) {
        LinkedHashMap<String, Object> section = new LinkedHashMap<>();
        put(path, section);
        return new BungeeBackendConfiguration(path, section);
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return get(path) instanceof Map<?, ?>;
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        if (!deep) {
            return new LinkedHashMap<>(data);
        }
        return deepCopy(data);
    }

    private Object get(String path) {
        String[] parts = path.split("\\.");
        Object current = data;
        for (String part : parts) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private void put(String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map<?, ?>)) {
                LinkedHashMap<String, Object> newSection = new LinkedHashMap<>();
                current.put(parts[i], newSection);
                current = newSection;
            } else {
                current = (Map<String, Object>) next;
            }
        }
        current.put(parts[parts.length - 1], value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepCopy(Map<String, Object> source) {
        LinkedHashMap<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> map) {
                out.put(entry.getKey(), deepCopy((Map<String, Object>) map));
            } else if (value instanceof List<?> list) {
                out.put(entry.getKey(), new ArrayList<>(list));
            } else {
                out.put(entry.getKey(), value);
            }
        }
        return out;
    }
}
