package dev.rono.permissions.bungee.backends.memory;

import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;

import java.util.*;

final class BungeeMemoryData implements PermissionsGroupData, PermissionsUserData {
    private String identifier;
    private final Map<String, List<String>> permissions = new HashMap<>();
    private final Map<String, Map<String, String>> options = new HashMap<>();
    private final Map<String, List<String>> parents = new HashMap<>();

    BungeeMemoryData(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void load() {}

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public List<String> getPermissions(String worldName) {
        return permissions.getOrDefault(worldName, Collections.emptyList());
    }

    @Override
    public void setPermissions(List<String> permissions, String worldName) {
        this.permissions.put(worldName, List.copyOf(permissions));
    }

    @Override
    public Map<String, List<String>> getPermissionsMap() {
        return Collections.unmodifiableMap(permissions);
    }

    @Override
    public Set<String> getWorlds() {
        Set<String> worlds = new HashSet<>(permissions.keySet());
        worlds.addAll(options.keySet());
        worlds.addAll(parents.keySet());
        return worlds;
    }

    @Override
    public String getOption(String option, String worldName) {
        Map<String, String> worldOptions = options.get(worldName);
        return worldOptions == null ? null : worldOptions.get(option);
    }

    @Override
    public void setOption(String option, String value, String worldName) {
        Map<String, String> worldOptions = options.computeIfAbsent(worldName, k -> new HashMap<>());
        if (value == null) {
            worldOptions.remove(option);
        } else {
            worldOptions.put(option, value);
        }
    }

    @Override
    public Map<String, String> getOptions(String worldName) {
        Map<String, String> worldOptions = options.get(worldName);
        return worldOptions == null ? Collections.emptyMap() : Collections.unmodifiableMap(worldOptions);
    }

    @Override
    public Map<String, Map<String, String>> getOptionsMap() {
        return Collections.unmodifiableMap(options);
    }

    @Override
    public List<String> getParents(String worldName) {
        return parents.getOrDefault(worldName, Collections.emptyList());
    }

    @Override
    public void setParents(List<String> parents, String worldName) {
        this.parents.put(worldName, new ArrayList<>(parents));
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public void save() {}

    @Override
    public void remove() {
        permissions.clear();
        options.clear();
        parents.clear();
    }

    @Override
    public Map<String, List<String>> getParentsMap() {
        return Collections.unmodifiableMap(parents);
    }

    @Override
    public boolean setIdentifier(String identifier) {
        this.identifier = identifier;
        return true;
    }
}
