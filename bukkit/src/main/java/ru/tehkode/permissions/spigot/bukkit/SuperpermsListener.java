package ru.tehkode.permissions.spigot.bukkit;

import dev.rono.permissions.api.bus.EntityDispatch;
import dev.rono.permissions.api.bus.EntityMutation;
import dev.rono.permissions.api.bus.SystemDispatch;
import dev.rono.permissions.api.bus.SystemMutation;
import dev.rono.permissions.api.event.PermissionEventBus;
import dev.rono.permissions.api.event.PermissionEventListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PEX permissions database integration with superperms.
 *
 * <p>Permission mutations are observed via the modern {@link PermissionEventBus}; legacy Bukkit
 * events are only published for third-party hook plugins when the legacy bridge is active.</p>
 */
public class SuperpermsListener implements Listener {
	private final SpigotPermissionsExPlugin plugin;
	private final PermissionEventBus eventBus;
	private PermissionEventBus.Subscription busSubscription;
	private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

	public SuperpermsListener(SpigotPermissionsExPlugin plugin) {
		this.plugin = plugin;
		var api = ((SpigotPermissionManager) plugin.getPermissionsManager()).permissionsExApi();
		this.eventBus = api.getEventBus();
		this.busSubscription = eventBus.subscribe(new PermissionEventListener() {
			@Override
			public void onEntity(EntityDispatch dispatch) {
				handleEntityDispatch(dispatch);
			}

			@Override
			public void onSystem(SystemDispatch dispatch) {
				handleSystemDispatch(dispatch);
			}
		});
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			updateAttachment(player);
		}
	}

	protected void updateAttachment(Player player) {
		updateAttachment(player, player.getWorld().getName());
	}

	protected void updateAttachment(Player player, String worldName) {
		PermissionAttachment attach = attachments.get(player.getUniqueId());
		Permission playerPerm = getCreateWrapper(player, "");
		Permission playerOptionPerm = getCreateWrapper(player, ".options");
		if (attach == null) {
			attach = player.addAttachment(plugin);
			attachments.put(player.getUniqueId(), attach);
			attach.setPermission(playerPerm, true);
			attach.setPermission(playerOptionPerm, true);
		}

		PermissionUser user = plugin.getPermissionsManager().getUser(player.getUniqueId());
		if (user != null) {
			if (user.isDebug()) {
				plugin.getLogger().info("Updating superperms for player " + player.getName());
			}
			updatePlayerPermission(playerPerm, user, worldName);
			updatePlayerMetadata(playerOptionPerm, user, worldName);
			player.recalculatePermissions();
		}
	}

	private String permissionName(Player player, String suffix) {
		return "permissionsex.player." + player.getUniqueId().toString() + suffix;
	}

	private void removePEXPerm(Player player, String suffix) {
		plugin.getServer().getPluginManager().removePermission(permissionName(player, suffix));
	}

	private Permission getCreateWrapper(Player player, String suffix) {
		final String name = permissionName(player, suffix);
		Permission perm = plugin.getServer().getPluginManager().getPermission(name);
		if (perm == null) {
			perm = new Permission(name, "Internal permission for PEX. DO NOT SET DIRECTLY", PermissionDefault.FALSE) {
				@Override
				public void recalculatePermissibles() {
					// no-op
				}
			};
			plugin.getServer().getPluginManager().addPermission(perm);
		}

		return perm;

	}

	private void updatePlayerPermission(Permission permission, PermissionUser user, String worldName) {
		permission.getChildren().clear();
		for (String perm : user.getPermissions(worldName)) {
			boolean value = true;
			if (perm.startsWith("-")) {
				value = false;
				perm = perm.substring(1);
			}
			if (!permission.getChildren().containsKey(perm)) {
				permission.getChildren().put(perm, value);
			}
		}
	}

	private void updatePlayerMetadata(Permission rootPermission, PermissionUser user, String worldName) {
		rootPermission.getChildren().clear();
		final List<String> groups = user.getParentIdentifiers(worldName);
		final Map<String, String> options = user.getOptions(worldName);
		for (String group : groups) {
			rootPermission.getChildren().put("groups." + group, true);
			rootPermission.getChildren().put("group." + group, true);
		}

		for (Map.Entry<String, String> option : options.entrySet()) {
			rootPermission.getChildren().put("options." + option.getKey() + "." + option.getValue(), true);
		}

		rootPermission.getChildren().put("prefix." + user.getPrefix(worldName), true);
		rootPermission.getChildren().put("suffix." + user.getSuffix(worldName), true);

	}

	protected void removeAttachment(Player player) {
		PermissionAttachment attach = attachments.remove(player.getUniqueId());
		if (attach != null) {
			attach.remove();
		}

		removePEXPerm(player, "");
		removePEXPerm(player, ".options");
	}

	public void onDisable() {
		if (busSubscription != null) {
			eventBus.unsubscribe(busSubscription);
			busSubscription = null;
		}
		for (PermissionAttachment attach : attachments.values()) {
			attach.remove();
		}
		attachments.clear();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		try {
			updateAttachment(event.getPlayer());
		} catch (Throwable t) {
			ErrorReport.handleError("Superperms event join", t);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (!plugin.requiresLateUserSetup()) {
			handleLogin(event);
		}
	}

	@EventHandler
	public void onPlayerLoginLate(PlayerLoginEvent event) {
		if (plugin.requiresLateUserSetup()) {
			handleLogin(event);
		}
	}

	private void handleLogin(PlayerLoginEvent event) {
		try {
			final Player player = event.getPlayer();
			removeAttachment(player);
			updateAttachment(player, null);
		} catch (Throwable t) {
			ErrorReport.handleError("Superperms event login", t);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerLoginDeny(PlayerLoginEvent event) {
		if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
			try {
				removeAttachment(event.getPlayer());
				Player player = plugin.getServer().getPlayer(event.getPlayer().getUniqueId());
				if (player != null && player.isOnline()) {
					updateAttachment(player);
				}
			} catch (Throwable t) {
				ErrorReport.handleError("Superperms event login denied", t);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		try {
			removeAttachment(event.getPlayer());
		} catch (Throwable t) {
			ErrorReport.handleError("Superperms event quit", t);
		}
	}

	private void updateSelective(EntityMutation mutation, PermissionUser user) {
		final Player p = resolvePlayer(user);
		if (p != null) {
			switch (mutation) {
				case SAVED:
					break;

				case PERMISSIONS_CHANGED:
				case TIMEDPERMISSION_EXPIRED:
					if (user.isDebug()) {
						plugin.getLogger().info("Updating superperms permissions for player " + p.getName());
					}
					updatePlayerPermission(getCreateWrapper(p, ""), user, p.getWorld().getName());
					invalidatePermissibleCache(p);
					break;

				case OPTIONS_CHANGED:
				case INFO_CHANGED:
					if (user.isDebug()) {
						plugin.getLogger().info("Updating superperms metadata for player " + p.getName());
					}
					updatePlayerMetadata(getCreateWrapper(p, ".options"), user, p.getWorld().getName());
					invalidatePermissibleCache(p);
					break;

				default:
					updateAttachment(p);
			}
		}
	}

	private void invalidatePermissibleCache(Player player) {
		if (plugin.getRegexPerms() != null) {
			var permissible = plugin.getRegexPerms().getInjectedPermissible(player);
			if (permissible != null) {
				permissible.clearPermissionCache();
				return;
			}
		}
		player.recalculatePermissions();
	}

	private Player resolvePlayer(PermissionUser user) {
		try {
			Player p = plugin.getServer().getPlayer(UUID.fromString(user.getIdentifier()));
			if (p != null) {
				return p;
			}
		} catch (IllegalArgumentException ignored) {
		}
		return plugin.getServer().getPlayerExact(user.getName());
	}

	private void handleEntityDispatch(EntityDispatch dispatch) {
		try {
			if ("USER".equalsIgnoreCase(dispatch.entityType())) {
				PermissionUser user = plugin.getPermissionsManager().getUser(dispatch.entityIdentifier());
				if (user != null) {
					updateSelective(dispatch.mutation(), user);
				}
			} else if ("GROUP".equalsIgnoreCase(dispatch.entityType())) {
				PermissionGroup group = plugin.getPermissionsManager().getGroup(dispatch.entityIdentifier());
				if (group != null) {
					for (PermissionUser user : group.getActiveUsers(true)) {
						updateSelective(dispatch.mutation(), user);
					}
				}
			}
		} catch (Throwable t) {
			ErrorReport.handleError("Superperms event permission entity", t);
		}
	}

	@EventHandler
	public void onWorldChanged(PlayerChangedWorldEvent event) {
		try {
			updateAttachment(event.getPlayer());
		} catch (Throwable t) {
			ErrorReport.handleError("Superperms event world change", t);
		}
	}

	private void handleSystemDispatch(SystemDispatch dispatch) {
		try {
			if (dispatch.mutation() == SystemMutation.DEBUGMODE_TOGGLE
					|| dispatch.mutation() == SystemMutation.REINJECT_PERMISSIBLES) {
				return;
			}
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				updateAttachment(p);
			}
		} catch (Throwable t) {
			ErrorReport.handleError("Superperms event permission system event", t);
		}
	}
}
