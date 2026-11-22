package dev.rono.permissions.core.config;

import lombok.var;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.config.ConfigurationManager;
import dev.rono.permissions.core.platform.PlatformConfiguration;

@Value
@Accessors(fluent = true)
public class ConfigurationManagerImpl implements ConfigurationManager {
    GeneralConfiguration general;
    BackendConfiguration backend;
    AdvancedConfiguration advanced;

    public ConfigurationManagerImpl(GeneralConfiguration general, BackendConfiguration backend, AdvancedConfiguration advanced) {
        this.general = general;
        this.backend = backend;
        this.advanced = advanced;
    }

    public static ConfigurationManagerImpl load(PlatformConfiguration platform) {
        var general = GeneralConfiguration.load(platform);
        var advanced = AdvancedConfiguration.load(platform);
        var backend = BackendConfiguration.load(platform);

        return new ConfigurationManagerImpl(general, backend, advanced);
    }

    public boolean preloadOnJoin() {
        return advanced.preloadOnJoin();
    }

    public boolean vaultEnabled() {
        return general.vaultEnabled();
    }

    public boolean placeholderApiEnabled() {
        return general.placeholderApiEnabled();
    }

    /**
     * Whether players are refused when native permission checks cannot be taken
     * over.
     */
    public boolean nativePermissionsRequired() {
        return general.nativePermissionsRequired();
    }
}
