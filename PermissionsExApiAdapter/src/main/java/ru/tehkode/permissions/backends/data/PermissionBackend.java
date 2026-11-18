package ru.tehkode.permissions.backends.data;

import lombok.var;

import dev.rono.permissions.core.PexImplProvider;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;
import ru.tehkode.permissions.exceptions.PermissionBackendException;
import dev.rono.permissions.core.util.Immutables;

/**
 * Legacy PermissionsEx storage view backed by PermissionsExPlus managers.
 * Plus is resolved via {@code PexImplProvider.get()}.
 */
public class PermissionBackend extends ru.tehkode.permissions.backends.PermissionBackend {
    public PermissionBackend(PermissionManager manager, ConfigurationSection config) throws PermissionBackendException {
        super(manager, config);
    }

    @Override
    public int getSchemaVersion() {
        return 2;
    }

    @Override
    protected void setSchemaVersion(int version) {}

    @Override
    public void reload() {}

    @Override
    public PermissionsUserData getUserData(String userName) {
        return new UserData(userName);
    }

    @Override
    public PermissionsGroupData getGroupData(String groupName) {
        return new GroupData(groupName);
    }

    @Override
    public boolean hasUser(String userName) {
        return UserData.find(userName).isPresent();
    }

    @Override
    public boolean hasGroup(String group) {
        var api = PexImplProvider.get();

        return api.groups().find(group).toCompletableFuture().join().isPresent();
    }

    @Override
    public Collection<String> getUserIdentifiers() {
        var api = PexImplProvider.get();

        return api.users().cache().identifiers().stream().map(Object::toString).sorted().collect(Immutables.toList());
    }

    @Override
    public Collection<String> getUserNames() {
        var api = PexImplProvider.get();

        return api.users().cache().names().stream().sorted().collect(Immutables.toList());
    }

    @Override
    public Collection<String> getGroupNames() {
        var api = PexImplProvider.get();

        return api.groups().cache().identifiers();
    }

    @Override
    public List<String> getWorldInheritance(String world) {
        return PexImplProvider.get().worlds().parents(world);
    }

    @Override
    public Map<String, List<String>> getAllWorldInheritance() {
        return PexImplProvider.get().worlds().all();
    }

    @Override
    public void setWorldInheritance(String world, List<String> inheritance) {
        PexImplProvider.get().worlds().set(world, inheritance == null ? Immutables.listOf() : inheritance);
    }

    @Override
    public void writeContents(Writer writer) throws IOException {
        writer.write("# PermissionsEx compatibility view backed by PermissionsExPlus API\n");
        writer.write("groups: " + String.join(",", getGroupNames()) + "\n");
        writer.write("users: " + String.join(",", getUserIdentifiers()) + "\n");
    }
}
