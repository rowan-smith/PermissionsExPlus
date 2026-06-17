package dev.rono.permissions.paper;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin;
import ru.tehkode.permissions.spigot.bukkit.regexperms.PermissionList;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Installs regex-permission hooks through Paper's {@code overridePermissionManager} API so
 * {@link SimplePluginManager} final fields do not need to be replaced reflectively.
 */
public final class PaperPermissionBridge {
	private static final String PERMISSION_MANAGER_INTERFACE = "io.papermc.paper.plugin.PermissionManager";

	private final SpigotPermissionsExPlugin plugin;
	private final PermissionList permissionList;
	private final PluginManager pluginManager;

	private Object delegateManager;
	private boolean installed;

	public PaperPermissionBridge(SpigotPermissionsExPlugin plugin, PermissionList permissionList) {
		this.plugin = plugin;
		this.permissionList = permissionList;
		this.pluginManager = plugin.getServer().getPluginManager();
	}

	public static boolean isSupported(PluginManager pluginManager) {
		try {
			Class.forName(PERMISSION_MANAGER_INTERFACE);
			return pluginManager.getClass().getMethod(
					"overridePermissionManager",
					Plugin.class,
					Class.forName(PERMISSION_MANAGER_INTERFACE)
			) != null;
		} catch (ReflectiveOperationException ignored) {
			return false;
		}
	}

	public boolean install() {
		if (installed || !isSupported(pluginManager)) {
			return false;
		}
		try {
			delegateManager = readCurrentPermissionManager();
			if (delegateManager == null) {
				return false;
			}
			Object wrapper = createWrapper(delegateManager);
			invokeOverride(wrapper);
			registerExistingPermissions();
			installed = true;
			return true;
		} catch (ReflectiveOperationException e) {
			plugin.getLogger().log(Level.WARNING,
					"Unable to install Paper permission bridge; falling back to legacy field injection.", e);
			return false;
		}
	}

	public void uninstall() {
		if (!installed) {
			return;
		}
		try {
			invokeOverride(delegateManager);
		} catch (ReflectiveOperationException e) {
			plugin.getLogger().log(Level.WARNING, "Unable to restore Paper permission manager.", e);
		} finally {
			installed = false;
			delegateManager = null;
		}
	}

	public boolean isInstalled() {
		return installed;
	}

	private void registerExistingPermissions() {
		for (Permission permission : pluginManager.getPermissions()) {
			permissionList.trackPermission(permission);
		}
	}

	private Object readCurrentPermissionManager() throws ReflectiveOperationException {
		Object paperPluginManager = readPaperPluginManager();
		Field permissionManagerField = paperPluginManager.getClass().getDeclaredField("permissionManager");
		permissionManagerField.setAccessible(true);
		return permissionManagerField.get(paperPluginManager);
	}

	private Object readPaperPluginManager() throws ReflectiveOperationException {
		if (pluginManager instanceof SimplePluginManager simplePluginManager) {
			Field paperField = SimplePluginManager.class.getDeclaredField("paperPluginManager");
			paperField.setAccessible(true);
			return paperField.get(simplePluginManager);
		}
		throw new IllegalStateException("Unexpected plugin manager implementation: " + pluginManager.getClass().getName());
	}

	private void invokeOverride(Object permissionManager) throws ReflectiveOperationException {
		Class<?> permissionManagerType = Class.forName(PERMISSION_MANAGER_INTERFACE);
		Method override = pluginManager.getClass().getMethod("overridePermissionManager", Plugin.class, permissionManagerType);
		override.invoke(pluginManager, plugin, permissionManager);
	}

	private Object createWrapper(Object delegate) throws ReflectiveOperationException {
		Class<?> permissionManagerType = Class.forName(PERMISSION_MANAGER_INTERFACE);
		return Proxy.newProxyInstance(
				permissionManagerType.getClassLoader(),
				new Class<?>[]{permissionManagerType},
				new PermissionManagerHandler(delegate)
		);
	}

	private final class PermissionManagerHandler implements InvocationHandler {
		private final Object delegate;

		private PermissionManagerHandler(Object delegate) {
			this.delegate = delegate;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String name = method.getName();
			if ("addPermission".equals(name) && args != null && args.length == 1 && args[0] instanceof Permission permission) {
				permissionList.trackPermission(permission);
				return method.invoke(delegate, args);
			}
			if ("addPermissions".equals(name) && args != null && args.length == 1 && args[0] instanceof List<?> permissions) {
				for (Object permission : permissions) {
					if (permission instanceof Permission perm) {
						permissionList.trackPermission(perm);
					}
				}
				return method.invoke(delegate, args);
			}
			if ("removePermission".equals(name) && args != null && args.length == 1) {
				if (args[0] instanceof Permission permission) {
					permissionList.untrackPermission(permission);
				} else if (args[0] instanceof String permissionName) {
					Permission permission = pluginManager.getPermission(permissionName);
					if (permission != null) {
						permissionList.untrackPermission(permission);
					}
				}
				return method.invoke(delegate, args);
			}
			if ("clearPermissions".equals(name)) {
				permissionList.clearTracking();
				return method.invoke(delegate, args);
			}
			if ("getPermissionSubscriptions".equals(name) && args != null && args.length == 1 && args[0] instanceof String permission) {
				@SuppressWarnings("unchecked")
				Set<Permissible> subscribed = (Set<Permissible>) method.invoke(delegate, args);
				return expandRegexSubscriptions(permission, subscribed);
			}
			return method.invoke(delegate, args);
		}

		private Set<Permissible> expandRegexSubscriptions(String permission, Set<Permissible> subscribed) {
			Set<Permissible> result = new HashSet<>(subscribed);
			for (Player player : plugin.getServer().getOnlinePlayers()) {
				if (player.hasPermission(permission)) {
					result.add(player);
				}
			}
			return result;
		}
	}
}
