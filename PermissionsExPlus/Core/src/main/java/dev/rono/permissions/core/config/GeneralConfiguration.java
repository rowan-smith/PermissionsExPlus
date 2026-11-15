package dev.rono.permissions.core.config;

import lombok.var;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import java.nio.file.Files;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/** General runtime settings read through a real YAML parser. */
@Value
@Accessors(fluent = true)
public class GeneralConfiguration {
    String defaultGroup;
    boolean caseSensitive;
    boolean wildcardsEnabled;
    boolean allowNegations;
    boolean shorthandExpansionsEnabled;
    boolean verboseDebug;
    boolean vaultEnabled;
    boolean placeholderApiEnabled;

    public GeneralConfiguration(String defaultGroup, boolean caseSensitive, boolean wildcardsEnabled, boolean allowNegations, boolean shorthandExpansionsEnabled, boolean verboseDebug, boolean vaultEnabled, boolean placeholderApiEnabled) {
        this.defaultGroup = defaultGroup;
        this.caseSensitive = caseSensitive;
        this.wildcardsEnabled = wildcardsEnabled;
        this.allowNegations = allowNegations;
        this.shorthandExpansionsEnabled = shorthandExpansionsEnabled;
        this.verboseDebug = verboseDebug;
        this.vaultEnabled = vaultEnabled;
        this.placeholderApiEnabled = placeholderApiEnabled;
    }

    public GeneralConfiguration(String defaultGroup,
            boolean caseSensitive,
            boolean wildcardsEnabled,
            boolean allowNegations,
            boolean verboseDebug) {
        this(defaultGroup, caseSensitive, wildcardsEnabled, allowNegations, true, verboseDebug, true, true);
    }

    public GeneralConfiguration(String defaultGroup,
            boolean caseSensitive,
            boolean wildcardsEnabled,
            boolean allowNegations,
            boolean verboseDebug,
            boolean vaultEnabled) {
        this(defaultGroup, caseSensitive, wildcardsEnabled, allowNegations, true, verboseDebug, vaultEnabled, true);
    }

    public GeneralConfiguration(String defaultGroup,
            boolean caseSensitive,
            boolean wildcardsEnabled,
            boolean allowNegations,
            boolean verboseDebug,
            boolean vaultEnabled,
            boolean placeholderApiEnabled) {
        this(defaultGroup, caseSensitive, wildcardsEnabled, allowNegations, true, verboseDebug, vaultEnabled,
                placeholderApiEnabled);
    }

    public static GeneralConfiguration load(PlatformConfiguration platform) {
        var file = platform.resolve("config.yml");

        try {
            Files.createDirectories(file.getParent());

            if (Files.notExists(file)) {
                platform.saveResource("config.yml", false);
            }

            if (Files.notExists(file)) {
                throw new IllegalStateException("Bundled config.yml was not saved to " + file);
            }

            var root = YamlConfigurationLoader.builder().path(file).build().load();

            requireSupportedVersion("config-version", root.node("config-version").getInt(1));

            return new GeneralConfiguration(
                    root.node("default-group").getString("default"),
                    root.node("case-sensitive").getBoolean(false),
                    root.node("wildcards", "enabled").getBoolean(true),
                    root.node("wildcards", "allow-negations").getBoolean(true),
                    root.node("wildcards", "enable-shorthand-expansions").getBoolean(true),
                    root.node("debug", "verbose").getBoolean(false),
                    root.node("hooks", "vault", "enabled").getBoolean(true),
                    root.node("hooks", "placeholder-api").getBoolean(true));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load " + file, exception);
        }
    }

    private static void requireSupportedVersion(String key, int version) {
        if (version != 1) {
            throw new IllegalStateException("Unsupported " + key + " " + version + "; expected 1");
        }
    }
}
