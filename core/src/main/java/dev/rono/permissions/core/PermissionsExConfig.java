package dev.rono.permissions.core;

import dev.rono.permissions.core.config.PexConfigData;
import dev.rono.permissions.core.config.PexRef;

import ru.tehkode.permissions.PEXBackendConfiguration;
public interface PermissionsExConfig {
    /**
     * Current {@link PexConfigData} from {@code config.yml}.
     *
     * <p>For the standalone {@link dev.rono.permissions.core.config.PexPermissionsData permissions.yml store}, load that
     * file separately ({@link dev.rono.permissions.core.config.PexPermissionsData#fromRoot}) when parsing user/group data.</p>
     */
    PexRef<PexConfigData> options();

    /** Persists {@code permissions.backend} and refreshes snapshot where applicable. */
    void setDefaultBackend(String backendName);

    boolean isDebug();

    boolean allowOps();

    boolean userAddGroupsLast();

    String getDefaultBackend();

    boolean shouldLogPlayers();

    boolean createUserRecords();

    boolean saveDefaultGroup();

    boolean informPlayers();

    String getBasedir();

    PEXBackendConfiguration getBackendConfig(String backend);

    void save();
}
