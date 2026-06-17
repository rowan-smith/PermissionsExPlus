package dev.rono.permissions.bungee;

import dev.rono.permissions.api.runtime.NoOpPlatformEventBus;
import dev.rono.permissions.api.runtime.PlatformRuntime;
import dev.rono.permissions.bungee.platform.BungeePlatformAdapter;
import dev.rono.permissions.bungee.platform.BungeePlatformScheduler;
import dev.rono.permissions.core.DefaultPermissionManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import dev.rono.permissions.core.commands.CoreCloudCommandRegistrar;
import dev.rono.permissions.core.commands.CoreCloudPlatform;
import dev.rono.permissions.core.commands.CoreCommandService;
import dev.rono.permissions.core.commands.PexCloudCommands;
import dev.rono.permissions.proxy.commands.ProxyConfigBridge;
import dev.rono.permissions.runtime.legacy.ProxyLegacyBridgeController;
import dev.rono.permissions.runtime.startup.BungeePermissionBootstrapReporter;
import dev.rono.permissions.runtime.startup.ProxyPlatformInitializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;

/**
 * Bungee bootstrap wiring the permission engine through {@link PlatformRuntime}.
 */
public class BungeePermissionsExPlugin extends Plugin implements PermissionsEx.ProxyLegacyBridgeHost {
    private PermissionManager manager;
    private BungeePermissionsExConfig config;
    private CoreCommandService commandService;
    private StrippingBungeeCommandManager<CommandSender> cloudManager;
    private PlatformRuntime platformRuntime;
    private ProxyLegacyBridgeController legacyBridge;

    @Override
    public void onEnable() {
        try {
            var adapter = new BungeePlatformAdapter(this);
            var scheduler = new BungeePlatformScheduler(this);
            this.platformRuntime = PlatformRuntime.of(adapter, NoOpPlatformEventBus.INSTANCE, scheduler);
            var startup = ProxyPlatformInitializer.start(getDataFolder(), getLogger(), platformRuntime);
            this.config = startup.config();
            this.manager = startup.manager();
            this.legacyBridge = startup.legacyBridge();
            PermissionsEx.registerBridgeHost(this);
            maybeActivateLegacyBridge("startup scan");
            getProxy().getPluginManager().registerListener(this, new BungeePexPermissionBridge(manager));
            this.cloudManager = new StrippingBungeeCommandManager<>(
                    this,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity());
            this.commandService = PexCloudCommands.install(new PexCloudCommands.InstallRequest<>(
                            cloudManager,
                            CommandSender.class,
                            manager,
                            new BungeeSenderAdapter(),
                            config::reload,
                            new ProxyConfigBridge(config),
                            force -> "UUID conversion is not supported on Bungee.",
                            CoreCloudPlatform.PROXY,
                            config.options().current().commandFramework())
                    .withImportBridge(new BungeeImportBridge()));
            BungeePermissionBootstrapReporter.log(this, this.manager, legacyBridge.isActive());
        } catch (PermissionBackendException ex) {
            getLogger().severe("Failed to initialize PermissionsExPlus Bungee adapter: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onDisable() {
        PermissionsEx.clearBridgeHost();
        ProxyPlatformInitializer.shutdown(manager instanceof DefaultPermissionManager dpm ? dpm : null);
        manager = null;
        legacyBridge = null;
        commandService = null;
        cloudManager = null;
        platformRuntime = null;
        getLogger().info("PermissionsExPlus Bungee adapter disabled");
    }

    public PermissionManager getManager() {
        return manager;
    }

    public PlatformRuntime getPlatformRuntime() {
        return platformRuntime;
    }

    @Override
    public void ensureLegacyBridgeForHook(String reason) {
        if (legacyBridge != null) {
            legacyBridge.activate(reason, getLogger());
        }
    }

    private void maybeActivateLegacyBridge(String scanContext) {
        var hook = BungeeLegacyHookPluginDetector.findHook(getProxy().getPluginManager(), this);
        if (hook != null) {
            legacyBridge.activate("detected hook plugin '" + hook.name() + "' (" + scanContext + ")", getLogger());
        }
    }

    private final class BungeeSenderAdapter implements CoreCloudCommandRegistrar.SenderAdapter<CommandSender> {
        @Override
        public void reply(CommandSender sender, String message) {
            sender.sendMessage(new TextComponent(message));
        }

        @Override
        public String defaultWorld(CommandSender sender) {
            if (sender instanceof ProxiedPlayer p && p.getServer() != null) {
                return p.getServer().getInfo().getName();
            }
            return null;
        }

        @Override
        public PermissionUser actor(CommandSender sender) {
            if (sender instanceof ProxiedPlayer p) {
                return manager.getUser(p.getUniqueId());
            }
            return null;
        }

        @Override
        public String helpText() {
            return "PermissionsExPlus commands loaded. Use /pex help.";
        }

        @Override
        public String pluginVersion() {
            return getDescription().getVersion();
        }
    }

    private final class BungeeImportBridge implements CoreCommandService.ImportBridge {
        private final Yaml yaml;

        private BungeeImportBridge() {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            this.yaml = new Yaml(options);
        }

        @Override
        public boolean supports(String source) {
            if (source == null) {
                return false;
            }
            String normalized = source.trim().toLowerCase(Locale.ROOT);
            return normalized.equals("bungeecord") || normalized.equals("bungee");
        }

        @Override
        @SuppressWarnings("unchecked")
        public String importIntoPex(String source) {
            java.nio.file.Path proxyConfig = resolveProxyConfigPath();
            if (!java.nio.file.Files.exists(proxyConfig)) {
                return "BungeeCord config not found at " + proxyConfig;
            }

            Map<String, Object> root;
            try (InputStream in = java.nio.file.Files.newInputStream(proxyConfig)) {
                Object parsed = yaml.load(in);
                if (!(parsed instanceof Map<?, ?> parsedMap)) {
                    return "BungeeCord config was empty or malformed. Nothing imported.";
                }
                root = (Map<String, Object>) parsedMap;
            } catch (IOException ex) {
                throw new RuntimeException("Failed reading BungeeCord config: " + ex.getMessage(), ex);
            }

            Map<String, Object> permissionsNode = asMap(root.get("permissions"));
            Map<String, Object> groupsNode = asMap(root.get("groups"));
            if (permissionsNode.isEmpty() && groupsNode.isEmpty()) {
                return "No native BungeeCord permissions/groups found to import.";
            }

            int importedGroups = 0;
            int importedPermissions = 0;
            if (!permissionsNode.isEmpty()) {
                for (Map.Entry<String, Object> entry : permissionsNode.entrySet()) {
                    String groupName = entry.getKey();
                    List<String> imported = coerceToStringList(entry.getValue());
                    if (groupName == null || groupName.isBlank() || imported.isEmpty()) {
                        continue;
                    }
                    PermissionGroup group = manager.getGroup(groupName);
                    List<String> merged = new java.util.ArrayList<>(group.getOwnPermissions(null));
                    int before = merged.size();
                    for (String permission : imported) {
                        if (!permission.isBlank() && !merged.contains(permission)) {
                            merged.add(permission);
                        }
                    }
                    if (merged.size() != before) {
                        group.setPermissions(merged, null);
                        group.save();
                        importedPermissions += (merged.size() - before);
                    }
                    if ("default".equalsIgnoreCase(groupName)) {
                        group.setDefault(true, null);
                        group.save();
                    }
                    importedGroups++;
                }
            }

            int importedUsers = 0;
            int importedMemberships = 0;
            if (!groupsNode.isEmpty()) {
                for (Map.Entry<String, Object> entry : groupsNode.entrySet()) {
                    String userName = entry.getKey();
                    if (userName == null || userName.isBlank()) {
                        continue;
                    }
                    List<String> imported = coerceToStringList(entry.getValue());
                    if (imported.isEmpty()) {
                        continue;
                    }
                    PermissionUser user = manager.getUser(userName);
                    List<String> currentGroups = new java.util.ArrayList<>(user.getOwnParentIdentifiers(null));
                    int before = currentGroups.size();
                    for (String groupName : imported) {
                        if (groupName == null || groupName.isBlank() || currentGroups.contains(groupName)) {
                            continue;
                        }
                        user.addGroup(groupName, null);
                        currentGroups.add(groupName);
                    }
                    if (currentGroups.size() != before) {
                        importedMemberships += (currentGroups.size() - before);
                        user.save();
                    }
                    importedUsers++;
                }
            }

            root.remove("permissions");
            root.remove("groups");
            try (Writer writer = java.nio.file.Files.newBufferedWriter(proxyConfig)) {
                yaml.dump(root, writer);
            } catch (IOException ex) {
                throw new RuntimeException("Imported into PEX but failed writing BungeeCord config cleanup: " + ex.getMessage(), ex);
            }

            return "Imported BungeeCord config into PEX: groups="
                    + importedGroups
                    + ", permissions="
                    + importedPermissions
                    + ", users="
                    + importedUsers
                    + ", memberships="
                    + importedMemberships
                    + ". Removed native permissions/groups blocks from "
                    + proxyConfig;
        }

        private java.nio.file.Path resolveProxyConfigPath() {
            java.nio.file.Path dataPath = getDataFolder().toPath();
            java.nio.file.Path byLayout = dataPath.getParent() != null && dataPath.getParent().getParent() != null
                    ? dataPath.getParent().getParent().resolve("config.yml")
                    : dataPath.resolveSibling("config.yml");
            if (java.nio.file.Files.exists(byLayout)) {
                return byLayout;
            }
            return java.nio.file.Path.of("config.yml").toAbsolutePath().normalize();
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> asMap(Object value) {
            if (!(value instanceof Map<?, ?> map)) {
                return java.util.Collections.emptyMap();
            }
            Map<String, Object> out = new java.util.LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                out.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return out;
        }

        private List<String> coerceToStringList(Object value) {
            if (!(value instanceof List<?> list)) {
                return java.util.List.of();
            }
            LinkedHashSet<String> out = new LinkedHashSet<>();
            for (Object item : list) {
                if (item != null) {
                    String text = String.valueOf(item).trim();
                    if (!text.isEmpty()) {
                        out.add(text);
                    }
                }
            }
            return java.util.List.copyOf(out);
        }
    }
}
