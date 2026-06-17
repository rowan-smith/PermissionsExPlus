package dev.rono.permissions.core.storage.backend;

import com.google.common.collect.ImmutableSet;
import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.core.InternalPermissionManager;
import dev.rono.permissions.core.backends.AbstractPermissionBackend;
import dev.rono.permissions.core.storage.EffectiveUserCache;
import dev.rono.permissions.core.storage.LocalSqlExporter;
import dev.rono.permissions.core.storage.LocalSqlRepository;
import dev.rono.permissions.core.storage.migration.YamlToSqlMigrator;
import dev.rono.permissions.core.storage.model.Group;
import dev.rono.permissions.core.storage.model.Ladder;
import dev.rono.permissions.core.storage.model.User;
import dev.rono.permissions.core.storage.resolution.EffectiveUser;
import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default H2-backed permissions store. YAML is supported only through one-time migration.
 */
public final class LocalSqlBackend extends AbstractPermissionBackend {

    private final LocalSqlRepository repository;
    private final EffectiveUserCache effectiveUserCache = new EffectiveUserCache();
    private final AtomicReference<ImmutableSet<String>> userNamesCache = new AtomicReference<>(ImmutableSet.of());
    private final AtomicReference<ImmutableSet<String>> groupNamesCache = new AtomicReference<>(ImmutableSet.of());
    private final File databaseFile;
    private final File legacyYamlFile;

    public LocalSqlBackend(PermissionManager manager, PEXBackendConfiguration config) throws PermissionBackendException {
        super(manager, config);
        File baseDir = resolveBaseDirectory(manager);
        String databaseName = config.getString("database", "permissions");
        this.databaseFile = new File(baseDir, databaseName);
        String yamlName = config.getString("migration-source", "permissions.yml");
        this.legacyYamlFile = new File(baseDir, yamlName);

        this.repository = LocalSqlRepository.fileDatabase(databaseFile.getAbsolutePath());
        try {
            initializeStorage();
        } catch (Exception e) {
            throw new PermissionBackendException("Failed to initialize local SQL backend", e);
        }
        refreshNameCaches();
    }

    LocalSqlBackend(PermissionManager manager, LocalSqlRepository repository, File legacyYamlFile)
            throws PermissionBackendException {
        super(manager, new EmptyBackendConfiguration("local-test"));
        this.repository = repository;
        this.databaseFile = legacyYamlFile;
        this.legacyYamlFile = legacyYamlFile;
        refreshNameCaches();
    }

    public LocalSqlRepository repository() {
        return repository;
    }

    public EffectiveUserCache effectiveUserCache() {
        return effectiveUserCache;
    }

    public EffectiveUser resolveEffectiveUser(String userName, PermissionContext context) throws Exception {
        UUID userId = resolveUserId(userName);
        return effectiveUserCache.getOrResolve(
                userId,
                context,
                () -> loadUserUnchecked(userId),
                this::loadAllGroupsUnchecked,
                this::loadLaddersUnchecked);
    }

    private User loadUserUnchecked(UUID userId) {
        try {
            return repository.loadUser(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<Integer, Group> loadAllGroupsUnchecked() {
        try {
            return repository.loadAllGroups();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Ladder> loadLaddersUnchecked() {
        try {
            return repository.loadLadders();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getSchemaVersion() {
        try {
            return repository.getSchemaVersion();
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    protected void setSchemaVersion(int version) {
        try {
            repository.setSchemaVersion(version);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() throws PermissionBackendException {
        effectiveUserCache.invalidateAll();
        refreshNameCaches();
    }

    @Override
    public PermissionsUserData getUserData(String userName) {
        return new LocalSqlUserData(this, userName);
    }

    @Override
    public PermissionsGroupData getGroupData(String groupName) {
        return new LocalSqlGroupData(this, groupName);
    }

    @Override
    public boolean hasUser(String userName) {
        try {
            return repository.userExistsByName(userName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasGroup(String group) {
        try {
            return repository.findGroupId(group).isPresent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<String> getUserIdentifiers() {
        return userNamesCache.get();
    }

    @Override
    public Collection<String> getUserNames() {
        return userNamesCache.get();
    }

    @Override
    public Collection<String> getGroupNames() {
        return groupNamesCache.get();
    }

    @Override
    public List<String> getWorldInheritance(String world) {
        try {
            return repository.getWorldInheritance(world);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<String>> getAllWorldInheritance() {
        try {
            return repository.getAllWorldInheritance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setWorldInheritance(String world, List<String> inheritance) {
        try {
            repository.setWorldInheritance(world, inheritance);
            effectiveUserCache.invalidateAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeContents(Writer writer) throws IOException {
        try {
            LocalSqlExporter.exportYaml(repository, writer);
        } catch (Exception ex) {
            throw new IOException("Failed to export local SQL backend", ex);
        }
    }

    public void backupDatabase(File target) throws IOException {
        try {
            repository.backupToScript(target.getAbsolutePath().replace('\\', '/'));
        } catch (Exception ex) {
            throw new IOException("Failed to backup local database", ex);
        }
    }

    @Override
    public String diagnosticLabel() {
        return "h2:" + databaseFile.getName();
    }

    @Override
    protected void backupDatabase() throws IOException {
        File backup = new File(databaseFile.getParentFile(), databaseFile.getName() + "-backup.sql");
        backupDatabase(backup);
    }

    UUID resolveUserId(String userName) throws Exception {
        return repository.findUserByName(userName)
                .map(user -> user.getId())
                .orElseGet(() -> UUID.nameUUIDFromBytes(("OfflinePlayer:" + userName).getBytes(StandardCharsets.UTF_8)));
    }

    int resolveGroupId(String groupName) throws Exception {
        return repository.findGroupId(groupName).orElseGet(() -> {
            try {
                return repository.upsertGroup(groupName, 0, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    void markDirty(LocalSqlEntityData entity) {
        invalidateAfterSave(entity);
    }

    void invalidateAfterSave(LocalSqlEntityData entity) {
        if (entity instanceof LocalSqlUserData userData) {
            try {
                effectiveUserCache.invalidateUser(resolveUserId(userData.getIdentifier()));
            } catch (Exception ignored) {
                effectiveUserCache.invalidateAll();
            }
        } else if (entity instanceof LocalSqlGroupData groupData) {
            try {
                effectiveUserCache.invalidateGroup(resolveGroupId(groupData.getIdentifier()));
            } catch (Exception ignored) {
                effectiveUserCache.invalidateAll();
            }
        }
        refreshNameCaches();
    }

    private void initializeStorage() throws Exception {
        if (!repository.isInitialized()) {
            repository.deploySchema();
        }
        repository.ensureSchemaLatest();
        YamlToSqlMigrator migrator = new YamlToSqlMigrator(getManager().getLogger());
        migrator.migrate(legacyYamlFile, repository);
    }

    private void refreshNameCaches() {
        try {
            userNamesCache.set(ImmutableSet.copyOf(repository.listUserNames()));
            groupNamesCache.set(ImmutableSet.copyOf(repository.listGroupNames()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static File resolveBaseDirectory(PermissionManager manager) throws PermissionBackendException {
        try {
            String baseDir =
                    Objects.requireNonNull(InternalPermissionManager.require(manager).getBasedir(), "basedir");
            File bd = new File(baseDir);
            if (!bd.exists() && !bd.mkdirs()) {
                throw new PermissionBackendException("Cannot create PermissionsEx base directory " + bd);
            }
            return bd;
        } catch (RuntimeException e) {
            throw new PermissionBackendException("Invalid basedir configuration", e);
        }
    }

    @Override
    public void close() throws PermissionBackendException {
        repository.close();
        try {
            super.close();
        } catch (PermissionBackendException e) {
            throw e;
        }
    }

    private static final class EmptyBackendConfiguration implements PEXBackendConfiguration {
        private final String name;

        private EmptyBackendConfiguration(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getString(String path) {
            return null;
        }

        @Override
        public String getString(String path, String def) {
            return def;
        }

        @Override
        public void set(String path, Object value) {}

        @Override
        public List<String> getStringList(String path) {
            return List.of();
        }

        @Override
        public PEXBackendConfiguration getConfigurationSection(String path) {
            return null;
        }

        @Override
        public PEXBackendConfiguration createSection(String path) {
            return this;
        }

        @Override
        public boolean isConfigurationSection(String path) {
            return false;
        }

        @Override
        public Map<String, Object> getValues(boolean deep) {
            return Map.of();
        }
    }
}
