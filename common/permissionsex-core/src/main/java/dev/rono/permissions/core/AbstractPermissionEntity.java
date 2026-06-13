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
package dev.rono.permissions.core;

import dev.rono.permissions.api.bus.EntityMutation;
import dev.rono.permissions.api.runtime.PlatformAdapter;
import ru.tehkode.permissions.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author code
 */
abstract class AbstractPermissionEntity implements PermissionEntity {
	protected PermissionManager manager;
	private String name;
	protected Map<String, List<String>> timedPermissions = new ConcurrentHashMap<>();
	protected Map<String, Long> timedPermissionsTime = new ConcurrentHashMap<>();
	protected boolean debugMode = false;

	protected AbstractPermissionEntity(String name, PermissionManager manager) {
		this.manager = manager;
		this.name = name;
	}

	protected abstract PermissionsData getData();

	@Override
	public abstract void clearCache();

	/**
	 * This method 100% run after all constructors have been run and entity
	 * object, and entity object are completely ready to operate
	 */
	public void initialize() {
		this.debugMode = this.getOptionBoolean("debug", null, this.debugMode);
	}

	@Override
	public PermissionManager getPermissionManager() {
		return manager;
	}

	/**
	 * Return name of permission entity (User or Group)
	 * User should be equal to Player's name on the server
	 *
	 * @return name
	 */
	public String getIdentifier() {
		return this.name;
	}

	public String getName() {
		return getOwnOption("name", null, getIdentifier());
	}

	/*protected void setName(String name) {
		setOption("name", name);
	}*/

	public abstract PermissionEntity.Type getType();

	/**
	 * Return non-inherited user prefix.
	 * This means if at entity  don't have has own prefix
	 * then empty string or null would be returned
	 *
	 * @return prefix as string
	 */
	public String getOwnPrefix() {
		return this.getOwnPrefix(null);
	}

	public String getOwnPrefix(String worldName) {
		return getOwnOption("prefix", worldName);
	}

	/**
	 * Return non-inherited user suffix.
	 * This means if an entity don't has own suffix
	 * then empty string or null would be returned
	 *
	 * @return suffix as string
	 */
	public final String getOwnSuffix() {
		return this.getOwnSuffix(null);
	}

	public String getOwnSuffix(String worldName) {
		return getOwnOption("suffix", worldName);
	}

	/**
	 * Returns entity prefix
	 *
	 * @param worldName
	 * @return prefix
	 */
	public String getPrefix(String worldName) {
		String ret = new HierarchyTraverser<String>(this, worldName) {
			@Override
			protected String fetchLocal(PermissionEntity entity, String world) {
				final String ret = entity.getOwnPrefix(world);
				return ret == null || ret.isEmpty() ? null : ret;
			}
		}.traverse();
		return ret == null ? "" : ret;
	}

	/**
	 * Returns entity prefix
	 *
	 */	public String getPrefix() {
		return this.getPrefix(null);
	}


	/**
	 * Set prefix to value
	 *
	 * @param prefix new prefix
	 */
	public void setPrefix(String prefix, String worldName) {
		getData().setOption("prefix", prefix, worldName);
		clearCache();
		this.callEvent(EntityMutation.INFO_CHANGED);
	}

	/**
	 * Return entity suffix
	 *
	 * @return suffix
	 */
	public String getSuffix(String worldName) {
		String ret = new HierarchyTraverser<String>(this, worldName) {
			@Override
			protected String fetchLocal(PermissionEntity entity, String world) {
				final String ret = entity.getOwnSuffix(world);
				return ret == null || ret.isEmpty() ? null : ret;
			}
		}.traverse();
		return ret == null ? "" : ret;
	}

	public String getSuffix() {
		return getSuffix(null);
	}

	/**
	 * Set suffix to value
	 *
	 * @param suffix new suffix
	 */
	public void setSuffix(String suffix, String worldName) {
		getData().setOption("suffix", suffix, worldName);
		clearCache();
		this.callEvent(EntityMutation.INFO_CHANGED);
	}

	/**
	 * Checks if entity has specified permission in default world
	 *
	 * @param permission Permission to check
	 * @return true if entity has this permission otherwise false
	 */
	public boolean has(String permission) {
		java.util.Collection<String> worlds = InternalPermissionManager.require(manager).getWorldNames();
		return this.has(permission, worlds.isEmpty() ? null : worlds.iterator().next());
	}

	/**
	 * Check if entity has specified permission in world
	 *
	 * @param permission Permission to check
	 * @param world      World to check permission in
	 * @return true if entity has this permission otherwise false
	 */
	public boolean has(String permission, String world) {
		if (permission != null && permission.isEmpty()) { // empty permission for public access :)
			return true;
		}

		String expression = getMatchingExpression(permission, world);

		if (this.isDebug()) {
			manager.getLogger().info("User " + this.getIdentifier() + " checked for \"" + permission + "\", " + (expression == null ? "no permission found" : "\"" + expression + "\" found"));
		}

		return explainExpression(expression);
	}

	/**
	 * Return all entity permissions in specified world
	 *
	 * @param world World name
	 * @return Array of permission expressions
	 */
	public List<String> getPermissions(String world) {
		return Collections.unmodifiableList(getPermissionsInternal(world));
	}

	public List<String> getPermissions() {
		return getPermissions(null);
	}

	/**
	 * Returns own (without inheritance) permissions of group for world
	 *
	 * @param world world's world name
	 * @return Array of permissions for world
	 */	public List<String> getOwnPermissions(String world) {
		return Collections.unmodifiableList(getData().getPermissions(world));
	}

	/**
	 * Return permissions for all worlds
	 * Common permissions stored as "" (empty string) as world.
	 *
	 * @return Map with world name as key and permissions array as value
	 */
	public Map<String, List<String>> getAllPermissions() {
		Map<String, List<String>> ret = new HashMap<>(getData().getPermissionsMap());
		for (Map.Entry<String, List<String>> timedEnt : timedPermissions.entrySet()) {
			String worldKey = timedEnt.getKey().isEmpty() ? null : timedEnt.getKey();
			List<String> addTo = new LinkedList<>();
			addTo.addAll(timedEnt.getValue());
			List<String> permanentPerms = ret.get(worldKey);
			if (permanentPerms != null) {
				addTo.addAll(permanentPerms);
			}
			ret.put(worldKey, Collections.unmodifiableList(addTo));
		}
		return Collections.unmodifiableMap(ret);
	}

	protected List<String> getPermissionsInternal(String worldName) {
		final List<String> ret = new ArrayList<>();

		new HierarchyTraverser<Void>(this, worldName) {
			@Override
			protected Void fetchLocal(PermissionEntity entity, String world) {
				for (String perm : entity.getOwnPermissions(world)) {
					if (perm.startsWith(PermissionEntity.NON_INHERITABLE_PREFIX) && !AbstractPermissionEntity.this.getParents(world).contains(entity)) {
						continue;
					}

					ret.add(perm);
					((AbstractPermissionEntity) entity).getInheritedChildPermissions(perm, ret);
				}

				for (String perm : entity.getTimedPermissions(world)) {
					if (perm.startsWith(PermissionEntity.NON_INHERITABLE_PREFIX) && !AbstractPermissionEntity.this.getParents(world).contains(entity)) {
						continue;
					}

					ret.add(perm);
					((AbstractPermissionEntity) entity).getInheritedChildPermissions(perm, ret);
				}
				return null;
			}
		}.traverse();

		return ret;
	}

	protected void getInheritedChildPermissions(String perm, List<String> list) {
		getInheritedChildPermissions(perm, list, false);
	}

	protected void getInheritedChildPermissions(String perm, List<String> list, boolean invert) {
		if (perm.startsWith("-")) {
			invert = !invert;
			perm = perm.substring(1);
		}
		if (perm.startsWith(PermissionEntity.NON_INHERITABLE_PREFIX)) {
			perm = perm.substring(1);
		}
		if (perm.contains("*") || perm.contains("(")) {
			return;
		}
		String prefix = perm + ".";
		for (String own : getOwnPermissions(null)) {
			if (own.startsWith(prefix) && !list.contains(own)) {
				list.add(invert ? "-" + own : own);
			}
		}
	}

	/**
	 * Add permissions for specified world
	 *
	 * @param permission Permission to add
	 * @param worldName      World name to add permission to
	 */
	public void addPermission(String permission, String worldName) {
		LinkedList<String> permissions = new LinkedList<>(this.getOwnPermissions(worldName));

		if (permissions.contains(permission)) { // remove old permission
			permissions.remove(permission);
		}

		// add permission on the top of list
		permissions.addFirst(permission);

		this.setPermissions(permissions, worldName);
	}

	/**
	 * Add permission in common space (all worlds)
	 *
	 * @param permission Permission to add
	 */
	public void addPermission(String permission) {
		this.addPermission(permission, null);
	}

	/**
	 * Remove permission in world
	 *
	 * @param permission Permission to remove
	 * @param worldName      World name to remove permission for
	 */
	public void removePermission(String permission, String worldName) {
		removeTimedPermission(permission, worldName);
		List<String> permissions = new LinkedList<>(this.getOwnPermissions(worldName));
		permissions.remove(permission);
		this.setPermissions(permissions, worldName);
	}

	/**
	 * Remove specified permission from all worlds
	 *
	 * @param permission Permission to remove
	 */
	public void removePermission(String permission) {
		for (String world : this.getAllPermissions().keySet()) {
			this.removePermission(permission, world);
		}
	}

	/**
	 * Set permissions in world
	 *
	 * @param permissions Array of permissions to set
	 * @param world       World to set permissions for
	 */
	public void setPermissions(List<String> permissions, String world) {
		getData().setPermissions(permissions, world);
		clearCache();
		this.callEvent(EntityMutation.PERMISSIONS_CHANGED);
	}

	/**
	 * Set specified permissions in common space (all world)
	 *
	 * @param permission Permission to set
	 */
	public void setPermissions(List<String> permission) {
		this.setPermissions(permission, null);
	}

	/**
	 * Get option in world
	 *
	 * @param option       Name of option
	 * @param world        World to look for
	 * @param defaultValue Default value to fallback if no such option was found
	 * @return Value of option as String
	 */
	public String getOption(final String option, String world, String defaultValue) {
		String ret = new HierarchyTraverser<String>(this, world) {
			@Override
			protected String fetchLocal(PermissionEntity entity, String world) {
				return entity.getOwnOption(option, world, null);
			}
		}.traverse();

		if (ret == null) {
			ret = defaultValue;
		}

		return ret;
	}

	/**
	 * Return option
	 * Option would be looked up in common options
	 *
	 * @param option Option name
	 * @return option value or empty string if option was not found
	 */
	public String getOption(String option) {
		return this.getOption(option, null, null);
	}

	/**
	 * Return option for world
	 *
	 * @param option Option name
	 * @param world  World to look in
	 * @return option value or empty string if option was not found
	 */
	public String getOption(String option, String world) {
		return this.getOption(option, world, null);
	}

	/**
	 * Return integer value for option
	 *
	 * @param optionName
	 * @param world
	 * @param defaultValue
	 * @return option value or defaultValue if option was not found or is not integer
	 */
	public int getOptionInteger(String optionName, String world, int defaultValue) {
		try {
			return Integer.parseInt(this.getOption(optionName, world, Integer.toString(defaultValue)));
		} catch (NumberFormatException ignore) {
		}

		return defaultValue;
	}

	/**
	 * Returns double value for option
	 *
	 * @param optionName
	 * @param world
	 * @param defaultValue
	 * @return option value or defaultValue if option was not found or is not double
	 */
	public double getOptionDouble(String optionName, String world, double defaultValue) {
		String option = this.getOption(optionName, world, Double.toString(defaultValue));

		try {
			return Double.parseDouble(option);
		} catch (NumberFormatException ignore) {
		}

		return defaultValue;
	}

	/**
	 * Returns boolean value for option
	 *
	 * @param optionName
	 * @param world
	 * @param defaultValue
	 * @return option value or defaultValue if option was not found or is not boolean
	 */
	public boolean getOptionBoolean(String optionName, String world, boolean defaultValue) {
		String option = this.getOption(optionName, world, Boolean.toString(defaultValue));

		if ("false".equalsIgnoreCase(option)) {
			return false;
		} else if ("true".equalsIgnoreCase(option)) {
			return true;
		}

		return defaultValue;
	}

	/**
	 * Set specified option in world
	 *
	 * @param option Option name
	 * @param value  Value to set, null to remove
	 * @param world  World name
	 */
	public void setOption(String option, String value, String world) {
		getData().setOption(option, value, world);
		clearCache();
		this.callEvent(EntityMutation.OPTIONS_CHANGED);
	}

	/**
	 * Set option for all worlds. Can be overwritten by world specific option
	 *
	 * @param option Option name
	 * @param value  Value to set, null to remove
	 */
	public void setOption(String option, String value) {
		this.setOption(option, value, null);
	}

	/**
	 * Get options in world
	 *
	 * @param world
	 * @return Option value as string Map
	 */
	public Map<String, String> getOptions(String world) {
		return getData().getOptions(world);
	}

	public Map<String, String> getOptions() {
		return getOptions(null);
	}

	/**
	 * Return options for all worlds
	 * Common options stored as null (empty string) as world.
	 *
	 * @return Map with world name as key and map of options as value
	 */
	public Map<String, Map<String, String>> getAllOptions() {
		return getData().getOptionsMap();
	}

	/**
	 * Return non-inherited value of specified option for user in world
	 *
	 * @param option       option string
	 * @param world        world's name
	 * @param defaultValue default value
	 * @return option value or defaultValue if option is not set
	 */
	public String getOwnOption(String option, String world, String defaultValue) {
		String ret = getData().getOption(option, world);
		if (ret == null) {
			return defaultValue;
		}
		return ret;
	}

	/**
	 * Return non-inherited value of specified option in common space (all worlds).
	 *
	 * @param option
	 * @return option value or empty string if option is not set
	 */
	public String getOwnOption(String option) {
		return this.getOwnOption(option, null, null);
	}

	public String getOwnOption(String option, String world) {
		return this.getOwnOption(option, world, null);
	}

	public int getOwnOptionInteger(String optionName, String world, int defaultValue) {
		String option = this.getOwnOption(optionName, world, Integer.toString(defaultValue));

		try {
			return Integer.parseInt(option);
		} catch (NumberFormatException ignore) {
		}

		return defaultValue;
	}

	public boolean getOwnOptionBoolean(String optionName, String world, boolean defaultValue) {
		String option = this.getOwnOption(optionName, world, Boolean.toString(defaultValue));

		if ("false".equalsIgnoreCase(option)) {
			return false;
		} else if ("true".equalsIgnoreCase(option)) {
			return true;
		}

		return defaultValue;
	}

	public double getOwnOptionDouble(String optionName, String world, double defaultValue) {
		String option = this.getOwnOption(optionName, world, Double.toString(defaultValue));

		try {
			return Double.parseDouble(option);
		} catch (NumberFormatException ignore) {
		}

		return defaultValue;
	}

	/**
	 * Save in-memory data to storage backend
	 */
	public void save() {
		getData().save();
		clearCache();
		this.callEvent(EntityMutation.SAVED);
	}

	/**
	 * Remove entity data from backend
	 */
	public void remove() {
		getData().remove();
		clearCache();
		this.callEvent(EntityMutation.REMOVED);
	}

	/**
	 * Return state of entity
	 *
	 * @return true if entity is only in-memory
	 */
	public boolean isVirtual() {
		return getData().isVirtual();
	}

	/**
	 * Return world names where entity have permissions/options/etc
	 *
	 * @return
	 */
	public Set<String> getWorlds() {
		return getData().getWorlds();
	}

	/**
	 * Return entity timed (temporary) permission for world
	 *
	 * @param world
	 * @return Array of timed permissions in that world
	 */
	public List<String> getTimedPermissions(String world) {
		if (world == null) {
			world = "";
		}

		if (!this.timedPermissions.containsKey(world)) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableList(this.timedPermissions.get(world));
	}

	/**
	 * Returns remaining lifetime of specified permission in world
	 *
	 * @param permission Name of permission
	 * @param world
	 * @return remaining lifetime in seconds of timed permission. 0 if permission is transient
	 */
	public int getTimedPermissionLifetime(String permission, String world) {
		if (world == null) {
			world = "";
		}

		if (!this.timedPermissionsTime.containsKey(world + ":" + permission)) {
			return 0;
		}

		return (int) (this.timedPermissionsTime.get(world + ":" + permission) - (System.currentTimeMillis() / 1000L));
	}

	/**
	 * Adds timed permission to specified world in seconds
	 *
	 * @param permission
	 * @param world
	 * @param lifeTime   Lifetime of permission in seconds. 0 for transient permission (world disappear only after server reload)
	 */
	public void addTimedPermission(final String permission, String world, int lifeTime) {
		if (world == null) {
			world = "";
		}

		if (!this.timedPermissions.containsKey(world)) {
			this.timedPermissions.put(world, new LinkedList<String>());
		}

		this.timedPermissions.get(world).add(permission);

		if (lifeTime > 0) {
			long expiry = (System.currentTimeMillis() / 1000L) + lifeTime;
			InternalPermissionManager.require(this.manager).timedExpiry().notifyEarliestExpiry(expiry);
			this.timedPermissionsTime.put(world + ":" + permission, expiry);
		}

		clearCache();
		this.callEvent(EntityMutation.PERMISSIONS_CHANGED);
	}

	/**
	 * Removes specified timed permission for world
	 *
	 * @param permission
	 * @param world
	 */
	public void removeTimedPermission(String permission, String world) {
		if (world == null) {
			world = "";
		}

		if (!this.timedPermissions.containsKey(world)) {
			return;
		}

		this.timedPermissions.get(world).remove(permission);
		this.timedPermissionsTime.remove(world + ":" + permission);

		clearCache();
		this.callEvent(EntityMutation.PERMISSIONS_CHANGED);
	}

	/**
	 * Expires timed permissions that ended at or before {@code nowEpochSecond}.
	 *
	 * @param nowEpochSecond current epoch second
	 * @return earliest future expiry among remaining timed permissions, or {@link Long#MAX_VALUE}
	 */
	long sweepTimedPermissions(long nowEpochSecond) {
		long nextExpiration = Long.MAX_VALUE;
		List<String> expiredKeys = new ArrayList<>();

		for (Map.Entry<String, Long> entry : this.timedPermissionsTime.entrySet()) {
			long expiry = entry.getValue();
			if (expiry <= nowEpochSecond) {
				expiredKeys.add(entry.getKey());
			} else {
				nextExpiration = Math.min(nextExpiration, expiry);
			}
		}

		for (String key : expiredKeys) {
			int colon = key.indexOf(':');
			String worldKey = colon >= 0 ? key.substring(0, colon) : "";
			String permission = colon >= 0 ? key.substring(colon + 1) : key;
			removeTimedPermission(permission, worldKey);
		}

		return nextExpiration;
	}

	/** Registers pending timed permission expirations with the shared coordinator. */
	void registerPendingTimedPermissionExpirations() {
		long now = System.currentTimeMillis() / 1000L;
		long next = sweepTimedPermissions(now);
		if (next < Long.MAX_VALUE) {
			InternalPermissionManager.require(this.manager).timedExpiry().notifyEarliestExpiry(next);
		}
	}

	protected void callEvent(EntityMutation action) {
		if (manager instanceof InternalPermissionManager internal) {
			internal.publishEntity(this.getIdentifier(), this.getType().toString(), action);
		}
	}

	protected PlatformAdapter platformAdapter() {
		return manager instanceof InternalPermissionManager internal
				? internal.getPlatform()
				: null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		final PermissionEntity other = (PermissionEntity) obj;
		return this.name.equals(other.getIdentifier());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(ident=" + this.getIdentifier() +  ",name=" + getName() + ")";
	}

	public String getMatchingExpression(String permission, String world) {
		return this.getMatchingExpression(this.getPermissions(world), permission);
	}

	public String getMatchingExpression(List<String> permissions, String permission) {
		for (String expression : permissions) {
			if (isMatches(expression, permission, true)) {
				return expression;
			}
		}

		return null;
	}

	/**
	 * Checks if specified permission matches specified permission expression
	 *
	 * @param expression       permission expression - what user have in his permissions list (permission.nodes.*)
	 * @param permission       permission which are checking for (permission.node.some.subnode)
	 * @param additionalChecks check for parent node matching
	 * @return
	 */
	public boolean isMatches(String expression, String permission, boolean additionalChecks) {
		return this.manager.getPermissionMatcher().isMatches(expression, permission);
	}

	public boolean explainExpression(String expression) {
		if (expression == null || expression.isEmpty()) {
			return false;
		}

		return !expression.startsWith("-"); // If expression have - (minus) before then that mean expression are negative
	}

	public boolean isDebug() {
		return this.debugMode || this.manager.isDebug();
	}

	public void setDebug(boolean debug) {
		this.debugMode = debug;
	}

	// -- Inheritance -- //
	public List<PermissionGroup> getOwnParents(String world) {
		List<PermissionGroup> ret = new ArrayList<>();
		for (String group : getOwnParentIdentifiers(world)) {
			ret.add(manager.getGroup(group));
		}
		Collections.sort(ret);
		return Collections.unmodifiableList(ret);
	}

	public List<PermissionGroup> getOwnParents() {
		return getOwnParents(null);
	}

	public List<String> getOwnParentIdentifiers(String world) {
		return Collections.unmodifiableList(getData().getParents(world));
	}

	public List<String> getOwnParentIdentifiers() {
		return getOwnParentIdentifiers(null);
	}

	public final List<PermissionGroup> getParents(String world) {
		return Collections.unmodifiableList(getParentsInternal(world));
	}

	public final List<PermissionGroup> getParents() {
		return getParents(null);
	}

	protected List<PermissionGroup> getParentsInternal(String world) {
		final List<PermissionGroup> ret = new ArrayList<>();
		new HierarchyTraverser<Void>(this, world, false) { // Must not traverse inheritance or bad things happen :)
			@Override
			protected Void fetchLocal(PermissionEntity entity, String world) {
				for (String groupName : entity.getOwnParentIdentifiers(world)) {
					if (groupName == null || groupName.trim().isEmpty()
							|| (AbstractPermissionEntity.this instanceof PermissionGroup && groupName.equalsIgnoreCase(getIdentifier()))) {
						continue;
					}

					PermissionGroup group = manager.getGroup(groupName);
					if (!ret.contains(group)) {
						ret.add(group);
					}
				}
				return null;
			}
		}.traverse();
		Collections.sort(ret);
		return ret;
	}

	public List<String> getParentIdentifiers(String world) {
		List<String> ret = new LinkedList<>();
		for (PermissionGroup group : getParentsInternal(world)) {
			ret.add(group.getIdentifier());
		}

		return Collections.unmodifiableList(ret);
	}

	/**
	 * Return names of parent groups in global scope
	 * @return Names of parent groups in unmodifiable list
	 */
	public List<String> getParentIdentifiers() {
		return getParentIdentifiers(null);
	}

	public Map<String, List<PermissionGroup>> getAllParents() {
		Map<String, List<PermissionGroup>> allGroups = new HashMap<>();

		for (String worldName : this.getWorlds()) {
			allGroups.put(worldName, this.getWorldParents(worldName));
		}
		allGroups.put(null, this.getWorldParents(null));

		return Collections.unmodifiableMap(allGroups);
	}

	protected List<PermissionGroup> getWorldParents(String worldName) {
		List<PermissionGroup> groups = new LinkedList<>();
		for (String groupName : getData().getParents(worldName)) {
			if (groupName == null || groupName.trim().isEmpty() || (this instanceof PermissionGroup && groupName.equalsIgnoreCase(this.getIdentifier()))) {
				continue;
			}

			PermissionGroup group = this.manager.getGroup(groupName);
			if (!groups.contains(group)) {
				groups.add(group);
			}
		}

		Collections.sort(groups);
		return Collections.unmodifiableList(groups);
	}

	public void setParents(List<PermissionGroup> parents, String world) {
		List<String> parentNames = new LinkedList<>();
		for (PermissionGroup group : parents) {
			parentNames.add(group.getIdentifier());
		}
		setParentsIdentifier(parentNames, world);
	}

	/**
	 * Set parents for entity in global namespace
	 * @param parents
	 */
	public void setParents(List<PermissionGroup> parents) {
		setParents(parents, null);
	}

	public void setParentsIdentifier(List<String> parentNames, String world) {
		getData().setParents(parentNames, world);
		clearCache();
		if (this instanceof PermissionUser user && manager instanceof DefaultPermissionManager dpm) {
			dpm.onUserGroupMembershipChanged(user, world);
		}
		this.callEvent(EntityMutation.INHERITANCE_CHANGED);
	}

	public void setParentsIdentifier(List<String> parentNames) {
		setParentsIdentifier(parentNames, null);
	}
}
