/*
 * PermissionsEx - Permissions plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ru.tehkode.permissions.spigot.bukkit;

import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;
import com.mojang.api.profiles.ProfileRepository;
import dev.rono.permissions.api.PermissionsExApi;
import dev.rono.permissions.api.runtime.PlatformRuntime;
import dev.rono.permissions.core.commands.CoreCloudCommandRegistrar;
import dev.rono.permissions.core.commands.CoreCloudPlatform;
import dev.rono.permissions.core.commands.CoreCommandService;
import dev.rono.permissions.runtime.startup.BukkitPermissionBootstrapReporter;
import dev.rono.permissions.spigot.platform.BukkitPlatformAdapter;
import dev.rono.permissions.spigot.platform.BukkitPlatformScheduler;
import dev.rono.permissions.spigot.platform.SpigotEventPublisher;
import dev.rono.permissions.spigot.platform.SpigotPlatformBridge;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import ru.tehkode.permissions.NativeInterface;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.PermissionsUserData;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.events.PermissionEvent;
import ru.tehkode.permissions.exceptions.PermissionBackendException;
import ru.tehkode.permissions.spigot.backends.FileBackend;
import ru.tehkode.permissions.spigot.backends.MemoryBackend;
import ru.tehkode.permissions.spigot.bukkit.regexperms.RegexPermissions;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

/**
 * @author code
 */
public class SpigotPermissionsExPlugin extends JavaPlugin implements NativeInterface {
    protected PermissionManager permissionsManager;
	private PermissionsExConfig config;
	protected SuperpermsListener superms;
	private RegexPermissions regexPerms;
    private StrippingBukkitCommandManager<CommandSender> cloudManager;
    private CoreCommandService coreCommandService;
	private boolean errored = false;
	private SpigotPlatformBridge platformBridge;
	private BukkitPlatformAdapter platformAdapter;
	private BukkitPlatformScheduler platformScheduler;
	private SpigotEventPublisher eventPublisher;
	private PlatformRuntime platformRuntime;

	public SpigotPermissionsExPlugin() {
		super();
		try {
			Field field = JavaPlugin.class.getDeclaredField("logger");
			field.setAccessible(true);
			field.set(this, new PermissionsExLogger(this));
		} catch (Exception e) {
			// Ignore, just hide the joke
		}

		PermissionBackend.registerBackendAlias("file", FileBackend.class);
		PermissionBackend.registerBackendAlias("memory", MemoryBackend.class);

	}

	private static class PermissionsExLogger extends PluginLogger {
		/**
		 * Protected method to construct a logger for a named subsystem.
		 * <p/>
		 * The logger will be initially configured with a null Level
		 * and with useParentHandlers set to true.
		 *
		 * @param plugin Plugin to get class info from
		 */
		protected PermissionsExLogger(Plugin plugin) {
			super(plugin);
			try {
				Field replace = PluginLogger.class.getDeclaredField("pluginName");
				replace.setAccessible(true);
				replace.set(this, "");
			} catch (Exception e) {
				// Dispose, if stuff happens the poor server admin just won't get their joke
			}

		}

		public boolean isDay() {
			final Calendar cal = Calendar.getInstance();
			return cal.get(Calendar.MONTH) == Calendar.APRIL && cal.get(Calendar.DAY_OF_MONTH) == 1;
		}

		@Override
		public void log(LogRecord record) {
			record.setMessage("[" + (isDay() ? "PermissionSex" : "PermissionsEx") + "] " + record.getMessage());
			super.log(record);
		}
	}

	private void logBackendExc(PermissionBackendException e) {
		getLogger().log(Level.SEVERE, "\n========== UNABLE TO LOAD PERMISSIONS BACKEND =========\n" +
									  "Your configuration must be fixed before PEX will enable\n" +
									  "Details: " + e.getMessage() + "\n" +
									  "=======================================================", e);
	}

	@Override
	public void onLoad() {
		try {
			this.config = new PermissionsExConfig(this);

			if (!getServer().getOnlineMode()) {
				getLogger().log(Level.WARNING, "This server is in offline mode. Unless this server is configured to integrate with a supported proxy (see http://dft.ba/-8ous), UUIDs *may not be stable*!");
			}
			//this.permissionsManager = new PermissionManager(this.config);
		/*} catch (PermissionBackendException e) {
			logBackendExc(e);
			errored = true;*/
		} catch (Throwable t) {
			ErrorReport.handleError("In onLoad", t);
			errored = true;
		}
	}

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		if (config != null) {
			config.refreshFromDisk();
		}
	}

	@Override
	public void onEnable() {
		if (errored) {
			getLogger().severe("==== PermissionsEx could not be enabled due to an earlier error. Look at the previous server log for more info ====");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		try {
			try {
				CacheBuilder.class.getMethod("maximumSize", long.class);
			} catch (NoSuchMethodException e) {
				getLogger().severe("=================================================================================");
				getLogger().severe("As of version 1.23, PEX is only compatible with versions of Minecraft 1.8 or greater. " +
						"Please downgrade to the most recent 1.22.x series version of PEX to continue.");
				getLogger().severe("=================================================================================");
				getPluginLoader().disablePlugin(this);
				return;
			}

			platformBridge = new SpigotPlatformBridge(this);
			platformAdapter = new BukkitPlatformAdapter(platformBridge);
			platformScheduler = new BukkitPlatformScheduler(this);
			eventPublisher = new SpigotEventPublisher(this, () -> permissionsManager);
			platformRuntime = PlatformRuntime.of(platformAdapter, eventPublisher, platformScheduler);
			if (!dev.rono.permissions.spigot.compat.ServerVersions.isWithinSupportedRange(getServer())) {
				getLogger().warning("Minecraft version " + dev.rono.permissions.spigot.compat.ServerVersions.minecraftVersion(getServer())
						+ " is outside the tested range "
						+ dev.rono.permissions.spigot.compat.ServerVersions.MIN_MC
						+ "–"
						+ dev.rono.permissions.spigot.compat.ServerVersions.MAX_MC
						+ "; continuing with compatibility shims enabled.");
			}
			if (this.permissionsManager == null) {
				this.permissionsManager = new SpigotPermissionManager(config, getLogger(), platformRuntime);
			}
            this.coreCommandService = new CoreCommandService(this.permissionsManager);

			try {
				OfflinePlayer.class.getMethod("getUniqueId");
			} catch (NoSuchMethodException e) {
				getLogger().severe("=================================================================================");
				getLogger().severe("As of version 1.21, PEX requires a version of Bukkit with UUID support to function (>1.7.5). Please download a non-UUID version of PermissionsEx to continue.");
				getLogger().severe("Beginning reversion of potential invalid UUID conversion");
				getPermissionsManager().getBackend().revertUUID();
				getLogger().severe("Reversion complete, disabling. PermissionsEx will not work until downgrade is complete");
				getLogger().severe("=================================================================================");
				getPluginLoader().disablePlugin(this);
				return;
			}

            try {
                this.cloudManager = new StrippingBukkitCommandManager<>(
                        this,
                        CommandExecutionCoordinator.simpleCoordinator(),
                        Function.identity(),
                        Function.identity());
                new CoreCloudCommandRegistrar<>(
                        cloudManager,
                        CommandSender.class,
                        coreCommandService,
                        new SpigotSenderAdapter(),
                        this::reloadConfig,
                        new SpigotConfigBridge(),
                        new SpigotUuidConversionBridge(),
                        CoreCloudPlatform.GAME_SERVER,
                        config.options().current().commandFramework())
                        .register();
            } catch (Exception cloudEx) {
                getLogger().warning("Failed to initialize Cloud command registration: " + cloudEx.getMessage());
            }

			// Register Player permissions cleaner
			PlayerEventsListener cleaner = new PlayerEventsListener();
			this.getServer().getPluginManager().registerEvents(cleaner, this);

			var permissionsExApi =
					((SpigotPermissionManager) this.permissionsManager).permissionsExApi();
			this.getServer().getServicesManager().register(PermissionManager.class, this.permissionsManager, this, ServicePriority.Normal);
			this.getServer().getServicesManager().register(
					PermissionsExApi.class,
					permissionsExApi,
					this,
					ServicePriority.Normal);
			regexPerms = new RegexPermissions(this);
			superms = new SuperpermsListener(this);
			this.getServer().getPluginManager().registerEvents(superms, this);
			this.saveConfig();

			// Start timed permissions cleaner timer
			this.permissionsManager.initTimer();

			BukkitPermissionBootstrapReporter.log(this, this.permissionsManager);

		} catch (PermissionBackendException e) {
			logBackendExc(e);
			this.getPluginLoader().disablePlugin(this);

		} catch (Throwable t) {
			ErrorReport.handleError("Error while enabling: ", t);
			this.getPluginLoader().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		try {
			if (this.permissionsManager != null) {
				var permissionsExApi =
						((SpigotPermissionManager) this.permissionsManager).permissionsExApi();
				this.permissionsManager.end();
				this.getServer().getServicesManager().unregister(PermissionsExApi.class, permissionsExApi);
				this.getServer().getServicesManager().unregister(PermissionManager.class, this.permissionsManager);
				this.permissionsManager = null;
			}
            this.cloudManager = null;
            this.coreCommandService = null;

			if (this.regexPerms != null) {
				this.regexPerms.onDisable();
				this.regexPerms = null;
			}
			if (this.superms != null) {
				this.superms.onDisable();
				this.superms = null;
			}

		} catch (Throwable t) {
			ErrorReport.handleError("While disabling", t);
		}
		ErrorReport.shutdown();
	}

	@Override
	public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String commandLabel, @NonNull String[] args) {
		if (cloudManager != null) {
			String commandLine = args.length == 0 ? command.getName() : command.getName() + " " + String.join(" ", args);
			cloudManager.executeCommand(sender, commandLine).whenComplete((result, ex) -> {
				if (ex == null) {
					return;
				}

				Throwable cause = ex;
				while (cause instanceof CompletionException && cause.getCause() != null) {
					cause = cause.getCause();
				}

				String msg = cause.getMessage();
				if (msg == null || msg.isEmpty()) {
					msg = "Command could not be parsed or executed.";
				}

				sender.sendMessage(ChatColor.RED + msg);
				getLogger().log(Level.FINE, "PEX command \"" + commandLine + "\" failed", cause);
			});

			return true;
		}

		if (args.length > 0) {
			return false;
		}

		sender.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + getDescription().getName()
				+ ChatColor.WHITE + "] v" + ChatColor.GREEN + getDescription().getVersion());
		return true;
	}

	@Override
	public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command,
			@NonNull String alias, @NonNull String[] args) {
		if (cloudManager == null) {
			return Collections.emptyList();
		}
		return cloudTabSuggest(sender, command, alias, args);
	}

	/**
	 * Builds the same command line Cloud expects; if the client omits a trailing empty argument after a
	 * space, retry with a trailing space so {@code pex user |} still completes usernames.
	 */
	private List<String> cloudTabSuggest(CommandSender sender, Command command, String alias, String[] args) {
		String root = alias != null && !alias.isEmpty() ? alias : command.getName();
		String line = args.length == 0 ? root : root + " " + String.join(" ", args);
		List<String> first = cloudManager.suggest(sender, line);
		if (!first.isEmpty()) {
			return first;
		}
		if (!line.endsWith(" ")) {
			List<String> spaced = cloudManager.suggest(sender, line + " ");
			if (!spaced.isEmpty()) {
				return spaced;
			}
		}
		return first;
	}

	public boolean requiresLateUserSetup() {
		return getServer().getPluginManager().isPluginEnabled("LilyPad-Connect");
	}

	public PermissionsExConfig getConfiguration() {
		return config;
	}

	public boolean isDebug() {
		return permissionsManager != null && permissionsManager.isDebug();
	}

	public RegexPermissions getRegexPerms() {
		return regexPerms;
	}

	public SuperpermsListener getSuperpermsListener() {
		return superms;
	}

	protected StrippingBukkitCommandManager<CommandSender> getCloudManager() {
		return cloudManager;
	}

	@Override
	public String UUIDToName(UUID uid) {
		return platformBridge.uuidToName(uid);
	}

	@Override
	public UUID nameToUUID(String name) {
		return platformBridge.nameToUuid(name);
	}

	@Override
	public boolean isOnline(UUID uuid) {
		return platformBridge.isOnline(uuid);
	}

	@Override
	public UUID getServerUUID() {
		return platformBridge.serverId();
	}

	@Override
	public void callEvent(PermissionEvent event) {
		eventPublisher.callEvent(event);
	}

	public PlatformRuntime getPlatformRuntime() {
		return platformRuntime;
	}

	public PermissionManager getPermissionsManager() {
		return permissionsManager;
	}

	public boolean has(Player player, String permission) {
		return this.permissionsManager.has(player, permission);
	}

	public boolean has(Player player, String permission, String world) {
		return this.permissionsManager.has(player, permission, world);
	}

	public void resetUser(Player player) {
		this.permissionsManager.resetUser(player);
	}

	public void clearUserCache(Player player) {
		this.permissionsManager.clearUserCache(player);
	}

    private final class SpigotSenderAdapter implements CoreCloudCommandRegistrar.SenderAdapter<CommandSender> {
        @Override
        public void reply(CommandSender sender, String message) {
            sender.sendMessage(message);
        }

        @Override
        public String defaultWorld(CommandSender sender) {
            if (sender instanceof Player p) {
                return p.getWorld().getName();
            }
            return null;
        }

        @Override
        public PermissionUser actor(CommandSender sender) {
            if (sender instanceof Player p) {
                return permissionsManager.getUser(p.getUniqueId());
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

        @Override
        public String reportText() {
            return "Create an issue report at https://github.com/PEXPlugins/PermissionsEx/issues";
        }

        @Override
        public String superPermsText(CommandSender sender, String user) {
            Player player;
            try {
                UUID uid = UUID.fromString(user);
                player = getServer().getPlayer(uid);
            } catch (IllegalArgumentException ex) {
                player = getServer().getPlayerExact(user);
            }
            if (player == null) {
                return "Player not found (offline?)";
            }
            return player.getEffectivePermissions().stream()
                    .map(info -> info.getPermission() + "=" + info.getValue())
                    .collect(Collectors.joining(", ", player.getName() + "'s superperms: ", ""));
        }
    }

    private final class SpigotConfigBridge implements CoreCommandService.ConfigBridge {
        @Override
        public Object get(String path) {
            return getConfig().get(path);
        }

        @Override
        public void set(String path, Object value) {
            getConfig().set(path, value);
        }

        @Override
        public void save() {
            saveConfig();
        }
    }

    private final class SpigotUuidConversionBridge implements CoreCommandService.UuidConversionBridge {
        @Override
        public String convert(boolean force) {
            final PermissionBackend backend = permissionsManager.getBackend();
            if (!getServer().getOnlineMode() && !force) {
                return "This server is running in offline mode and UUIDs may not be stable. Run '/pex convert uuid force' to continue.";
            }

            final ProfileRepository repo = new HttpProfileRepository("minecraft");
            final Collection<String> userIdentifiers = new HashSet<>(backend.getUserIdentifiers());
            for (Iterator<String> it = userIdentifiers.iterator(); it.hasNext(); ) {
                try {
                    UUID.fromString(it.next());
                    it.remove();
                } catch (IllegalArgumentException ignore) {
                }
            }

            if (userIdentifiers.isEmpty()) {
                return "No users to convert!";
            }

            backend.setPersistent(false);
            final Iterator<List<String>> splitIdentifiers = Iterables.partition(userIdentifiers, 50 * 1000).iterator();
            final AtomicInteger batchNum = new AtomicInteger(1);
            final int totalBatches = (int) Math.ceil(userIdentifiers.size() / 50000.0);

            platformScheduler.runAsync(new Runnable() {
                @Override
                public void run() {
                    List<String> names = splitIdentifiers.next();
                    try {
                        for (Profile profile : repo.findProfilesByNames(names.toArray(new String[0]))) {
                            PermissionsUserData data = backend.getUserData(profile.getName());
                            data.setIdentifier(profile.getId().replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                            data.setOption("name", profile.getName(), null);
                        }
                    } catch (Exception e) {
                        ErrorReport.handleError("While converting batch " + batchNum.get() + " to UUID", e);
                        backend.setPersistent(true);
                        return;
                    }

                    if (splitIdentifiers.hasNext()) {
                        platformScheduler.runLaterSeconds(this, TimeUnit.MINUTES.toSeconds(10));
                        getLogger().info("Completed conversion batch " + batchNum.getAndIncrement() + " of " + totalBatches);
                    } else {
                        getLogger().info("UUID conversion complete");
                        backend.setPersistent(true);
                    }
                }
            });

            return "Beginning conversion to UUID in " + totalBatches + " batches of max 50k (1 batch every 10 minutes)";
        }
    }

	public class PlayerEventsListener implements Listener {
		@EventHandler(priority = EventPriority.MONITOR)
		public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
			if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED && !requiresLateUserSetup()) {
				getPermissionsManager().cacheUser(event.getUniqueId().toString(), event.getName());
			}
		}

		@EventHandler
		public void onPlayerLogin(PlayerJoinEvent event) {
			try {
				PermissionUser user = getPermissionsManager().getUser(event.getPlayer().getUniqueId());
				if (!user.isVirtual()) {
					if (!event.getPlayer().getName().equals(user.getOption("name"))) { // Update name only if user exists in config
						user.setOption("name", event.getPlayer().getName());
					}
					if (!config.shouldLogPlayers()) {
						return;
					}
					user.setOption("last-login-time", Long.toString(System.currentTimeMillis() / 1000L));
					// user.setOption("last-login-ip", event.getPlayer().getAddress().getAddress().getHostAddress()); // somehow this won't work
				}
			} catch (Throwable t) {
				ErrorReport.handleError("While login cleanup event", t);
			}
		}

		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			try {
				PermissionUser user = getPermissionsManager().getUser(event.getPlayer().getUniqueId());
				if (!user.isVirtual()) {
					if (config.shouldLogPlayers()) {
						user.setOption("last-logout-time", Long.toString(System.currentTimeMillis() / 1000L));
					}

					user.getName(); // Set name if user was created during server run
				}
				PermissionManager pm = getPermissionsManager();
				pm.resetUser(event.getPlayer().getUniqueId().toString());
				pm.resetUser(event.getPlayer().getName());
			} catch (Throwable t) {
				ErrorReport.handleError("While logout cleanup event", t);
			}
		}
	}
}
