package ru.tehkode.permissions.spigot.bukkit.regexperms;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.events.PermissionSystemEvent;
import ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static ru.tehkode.permissions.spigot.bukkit.CraftBukkitInterface.getCBClassName;

/**
 * @author zml2008
 */
public class RegexPermissions {
	private final SpigotPermissionsExPlugin plugin;
	private PermissionList permsList;
	// Permissions subscriptions handling
	private PEXPermissionSubscriptionMap subscriptionHandler;
	private final Map<UUID, PermissiblePEX> injectedPermissibles = new ConcurrentHashMap<>();

	public RegexPermissions(SpigotPermissionsExPlugin plugin) {
		this.plugin = plugin;
		permsList = new PermissionList();
		if (!plugin.installRegexPermissionSubscription(this, permsList)) {
			subscriptionHandler = PEXPermissionSubscriptionMap.inject(plugin, plugin.getServer().getPluginManager());
			permsList = PermissionList.inject(plugin.getServer().getPluginManager());
		}
		plugin.getServer().getPluginManager().registerEvents(new EventListener(), plugin);
		injectAllPermissibles();
	}

	protected static final PermissibleInjector[] injectors = new PermissibleInjector[] {
			new PermissibleInjector.ClassPresencePermissibleInjector("net.glowstone.entity.GlowHumanEntity", "permissions", true),
			new PermissibleInjector.ClassPresencePermissibleInjector("org.getspout.server.entity.SpoutHumanEntity", "permissions", true),
			new PermissibleInjector.ClassNameRegexPermissibleInjector("org.getspout.spout.player.SpoutCraftPlayer", "perm", false, "org\\.getspout\\.spout\\.player\\.SpoutCraftPlayer"),
			new PermissibleInjector.ClassPresencePermissibleInjector(getCBClassName("entity.CraftHumanEntity"), "perm", true),
	};

	public void onDisable() {
		plugin.uninstallRegexPermissionSubscription(this);
		if (subscriptionHandler != null) {
			subscriptionHandler.uninject();
			subscriptionHandler = null;
		}
		uninjectAllPermissibles();
	}

	public boolean hasDebugMode() {
		PermissionManager manager = plugin.getPermissionsManager();
		return manager != null && manager.isDebug();
	}

	public PermissionList getPermissionList() {
		return permsList;
	}

	public PermissiblePEX getInjectedPermissible(Player player) {
		return injectedPermissibles.get(player.getUniqueId());
	}

	public void injectPermissible(Player player) {
		if (player.hasPermission("permissionsex.disabled")) { // this user shouldn't get permissionsex matching
			return;
		}

		try {
			PermissiblePEX permissible = new PermissiblePEX(player, plugin);

			boolean success = false, found = false;
			for (PermissibleInjector injector : injectors) {
				if (injector.isApplicable(player)) {
					found = true;
					Permissible oldPerm = injector.inject(player, permissible);
					if (oldPerm != null) {
						permissible.setPreviousPermissible(oldPerm);
						injectedPermissibles.put(player.getUniqueId(), permissible);
						success = true;
						break;
					}
				}
			}

			if (!found) {
				plugin.getLogger().warning("No Permissible injector found for your server implementation!");
			} else if (!success) {
				plugin.getLogger().warning("Unable to inject PEX's permissible for " + player.getName());
			}

			permissible.recalculatePermissions();

			if (success && hasDebugMode()) {
				plugin.getLogger().info("Permissions handler for " + player.getName() + " successfully injected");
			}
		} catch (Throwable e) {
			plugin.getLogger().log(Level.SEVERE, "Unable to inject permissible for " + player.getName(), e);
		}
	}

	private void injectAllPermissibles() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			injectPermissible(player);
		}
	}

	private void uninjectPermissible(Player player) {
		try {
			boolean success = false;
			for (PermissibleInjector injector : injectors) {
				if (injector.isApplicable(player)) {
					Permissible pexPerm = injector.getPermissible(player);
					if (pexPerm instanceof PermissiblePEX) {
						if (injector.inject(player, ((PermissiblePEX) pexPerm).getPreviousPermissible()) != null) {
							success = true;
							break;
						}
					} else {
						success = true;
						break;
					}
				}
			}

			injectedPermissibles.remove(player.getUniqueId());

			if (!success) {
				plugin.getLogger().warning("No Permissible injector found for your server implementation (while uninjecting for " + player.getName() + "!");
			} else if (hasDebugMode()) {
				plugin.getLogger().info("Permissions handler for " + player.getName() + " successfully uninjected");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void uninjectAllPermissibles() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			uninjectPermissible(player);
		}
	}

	private class EventListener implements Listener {
		@EventHandler(priority = EventPriority.LOWEST)
		public void onPlayerLogin(PlayerLoginEvent event) {
			injectPermissible(event.getPlayer());
		}

		@EventHandler(priority = EventPriority.MONITOR)
		// Technically not supposed to use MONITOR for this, but we don't want to remove before other plugins are done checking permissions
		public void onPlayerQuit(PlayerQuitEvent event) {
			uninjectPermissible(event.getPlayer());
		}

		@EventHandler(priority = EventPriority.LOWEST)
		public void onPermissionSystemEvent(PermissionSystemEvent event) {
			switch (event.getAction()) {
				case REINJECT_PERMISSIBLES:
				case RELOADED:
					uninjectAllPermissibles();
					injectAllPermissibles();
					break;
				default:
					return;
			}
		}
	}

}
