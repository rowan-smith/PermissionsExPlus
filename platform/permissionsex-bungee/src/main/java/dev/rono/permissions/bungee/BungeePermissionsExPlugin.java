package dev.rono.permissions.bungee;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import dev.rono.permissions.api.bus.PexPermissionDispatch;
import dev.rono.permissions.api.runtime.PlatformAdapter;
import dev.rono.permissions.api.service.PexPermissionService;
import dev.rono.permissions.bungee.backends.file.BungeeFileBackend;
import dev.rono.permissions.bungee.backends.memory.BungeeMemoryBackend;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.commands.CoreCloudCommandRegistrar;
import dev.rono.permissions.core.commands.CoreCloudPlatform;
import dev.rono.permissions.core.commands.CoreCommandService;
import dev.rono.permissions.runtime.startup.BungeePermissionBootstrapReporter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;

/**
 * Bungee bootstrap and {@link PlatformAdapter} bridge (proxy runtimes deliberately ignore synchronous Bukkit-domain dispatches).
 */
public class BungeePermissionsExPlugin extends Plugin implements PlatformAdapter {
    private static final UUID PROXY_UUID = UUID.nameUUIDFromBytes("permissionsexplus-bungee".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    private PermissionManager manager;
    private BungeePermissionsExConfig config;
    private CoreCommandService commandService;
    private StrippingBungeeCommandManager<CommandSender> cloudManager;

    @Override
    public void onEnable() {
        try {
            this.config = new BungeePermissionsExConfig(getDataFolder(), getLogger());
            PermissionBackend.registerBackendAlias("memory", BungeeMemoryBackend.class);
            PermissionBackend.registerBackendAlias("file", BungeeFileBackend.class);
            this.manager = new DefaultPermissionManager(config, getLogger(), this);
            getProxy().getPluginManager().registerListener(this, new BungeePexPermissionBridge(manager));
            this.commandService = new CoreCommandService(manager);
            this.cloudManager = new StrippingBungeeCommandManager<>(
                    this,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity());
            new CoreCloudCommandRegistrar<>(
                            cloudManager,
                            CommandSender.class,
                            commandService,
                            new BungeeSenderAdapter(),
                            config::reload,
                            new BungeeConfigBridge(),
                            force -> "UUID conversion is not supported on Bungee.",
                            new BungeeImportBridge(),
                            CoreCloudPlatform.PROXY)
                    .register();
            this.manager.initTimer();
            ProxyPermissionServices.register(
                    ((DefaultPermissionManager) this.manager).permissionsExApi(),
                    (PexPermissionService) this.manager,
                    this.manager);
            BungeePermissionBootstrapReporter.log(this, this.manager);
        } catch (PermissionBackendException ex) {
            getLogger().severe("Failed to initialize PermissionsExPlus Bungee adapter: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onDisable() {
        ProxyPermissionServices.unregister();
        if (manager != null) {
            manager.end();
            manager = null;
        }
        commandService = null;
        cloudManager = null;
        getLogger().info("PermissionsExPlus Bungee adapter disabled");
    }

    @Override
    public void publish(PexPermissionDispatch dispatch) {
        // Proxy runtimes intentionally do not emulate Bukkit event listeners.
    }

    @Override
    public String uuidToName(UUID uid) {
        ProxiedPlayer player = getProxy().getPlayer(uid);
        return player != null ? player.getName() : null;
    }

    @Override
    public UUID nameToUuid(String name) {
        ProxiedPlayer player = getProxy().getPlayer(name);
        return player != null ? player.getUniqueId() : null;
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return getProxy().getPlayer(uuid) != null;
    }

    @Override
    public UUID serverId() {
        return PROXY_UUID;
    }

    @Override
    public Collection<String> realmNames() {
        return getProxy().getServers().keySet();
    }

    @Override
    public String onlineRealm(UUID uuid) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        return player != null && player.getServer() != null ? player.getServer().getInfo().getName() : null;
    }

    @Override
    public String onlineDisplayName(UUID uuid) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        return player != null ? player.getName() : null;
    }

    @Override
    public boolean isOperator(UUID uuid) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        return player != null && player.hasPermission("permissionsex.admin");
    }

    public PermissionManager getManager() {
        return manager;
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

    private final class BungeeConfigBridge implements CoreCommandService.ConfigBridge {
        @Override
        public Object get(String path) {
            return config.getNode(path);
        }

        @Override
        public void set(String path, Object value) {
            config.setNode(path, value);
        }

        @Override
        public void save() {
            config.save();
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
