package dev.rono.permissions.bungee.backends.memory;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;
import dev.rono.permissions.core.backends.AbstractPermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
 * Bungee memory backend with no persistence.
 */
public class BungeeMemoryBackend extends AbstractPermissionBackend {
    private final Map<String, BungeeMemoryData> users = new ConcurrentHashMap<>();
    private final Map<String, BungeeMemoryData> groups = new ConcurrentHashMap<>();
    private final Map<String, List<String>> worldInheritance = new ConcurrentHashMap<>();

    public BungeeMemoryBackend(PermissionManager manager, PEXBackendConfiguration config)
            throws PermissionBackendException {
        super(manager, config);
    }

    @Override
    public int getSchemaVersion() {
        return -1;
    }

    @Override
    protected void setSchemaVersion(int version) {}

    @Override
    public void reload() throws PermissionBackendException {}

    @Override
    public PermissionsUserData getUserData(String userName) {
        return users.computeIfAbsent(userName.toLowerCase(), BungeeMemoryData::new);
    }

    @Override
    public PermissionsGroupData getGroupData(String groupName) {
        return groups.computeIfAbsent(groupName.toLowerCase(), BungeeMemoryData::new);
    }

    @Override
    public boolean hasUser(String userName) {
        return users.containsKey(userName.toLowerCase());
    }

    @Override
    public boolean hasGroup(String group) {
        return groups.containsKey(group.toLowerCase());
    }

    @Override
    public Collection<String> getUserIdentifiers() {
        return Collections.unmodifiableCollection(users.keySet());
    }

    @Override
    public Collection<String> getUserNames() {
        return Collections.unmodifiableCollection(users.keySet());
    }

    @Override
    public Collection<String> getGroupNames() {
        return Collections.unmodifiableCollection(groups.keySet());
    }

    @Override
    public List<String> getWorldInheritance(String world) {
        return worldInheritance.getOrDefault(world, Collections.emptyList());
    }

    @Override
    public Map<String, List<String>> getAllWorldInheritance() {
        return Collections.unmodifiableMap(worldInheritance);
    }

    @Override
    public void setWorldInheritance(String world, List<String> inheritance) {
        if (inheritance == null || inheritance.isEmpty()) {
            worldInheritance.remove(world);
        } else {
            worldInheritance.put(world, List.copyOf(inheritance));
        }
    }

    @Override
    public void writeContents(Writer writer) throws IOException {}
}
