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

import dev.rono.permissions.api.bus.EntityDispatch;
import dev.rono.permissions.api.bus.EntityMutation;
import dev.rono.permissions.api.bus.SystemDispatch;
import dev.rono.permissions.api.bus.SystemMutation;
import dev.rono.permissions.api.runtime.PlatformAdapter;
import dev.rono.permissions.api.service.PermissionService;
import ru.tehkode.permissions.PEXBackendConfiguration;
import dev.rono.permissions.core.backends.CorePermissionBackendRegistrar;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionMatcher;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;
/**
 * @author t3hk0d3
 */
public class DefaultPermissionManager implements PermissionManager, PermissionService, InternalPermissionManager {
	protected ConcurrentMap<String, PermissionUser> users = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, PermissionGroup> groups = new ConcurrentHashMap<>();
	protected PermissionBackend backend = null;
	private final PermissionsExConfig config;
	private final PlatformAdapter platform;
	private final Logger logger;
	protected ScheduledExecutorService executor;
	private final Map<String, ScheduledFuture<?>> clearTimedGroupsTasks = new HashMap<>();
	protected boolean debugMode = false;
	protected boolean allowOps = false;
	protected boolean userAddGroupsLast = false;

	protected PermissionMatcher matcher = new RegExpMatcher();
	private final GroupMembershipIndex groupMembershipIndex = new GroupMembershipIndex();

	public DefaultPermissionManager(PermissionsExConfig config, Logger logger, PlatformAdapter platform) throws PermissionBackendException {
		CorePermissionBackendRegistrar.ensureRegistered();
		this.config = config;
		this.logger = logger;
		this.platform = platform;
		this.debugMode = config.isDebug();
		this.allowOps = config.allowOps();
		this.userAddGroupsLast = config.userAddGroupsLast();
		this.initBackend();
	}

	UUID getServerUUID() {
		return platform.serverId();
	}

	public boolean shouldCreateUserRecords() {
		return config.createUserRecords();
	}

	public String getBasedir() {
		return config.getBasedir();
	}

	public void saveMainConfiguration() {
		config.save();
	}

	@Override
	public ru.tehkode.permissions.bukkit.PermissionsExConfig getConfiguration() {
		if (config instanceof ru.tehkode.permissions.bukkit.PermissionsExConfig legacy) {
			return legacy;
		}
		return new LegacyPermissionsExConfigAdapter(config);
	}

	@Override
	public PlatformAdapter getPlatform() {
		return platform;
	}

	@Override
	public boolean allowOps() {
		return allowOps;
	}

	@Override
	public boolean userAddGroupsLast() {
		return userAddGroupsLast;
	}

	@Override
	public void scheduleTimedGroupsCheck(long nextExpiration, final String identifier) {
		ScheduledFuture<?> future = clearTimedGroupsTasks.get(identifier);
		long newDelay = (nextExpiration - (System.currentTimeMillis() / 1000));

		if (future == null || future.isDone() || future.getDelay(TimeUnit.SECONDS) > newDelay) {
			clearTimedGroupsTasks.put(identifier, executor.schedule(new Runnable() {
				@Override
				public void run() {
					getUser(identifier).updateTimedGroups();
					clearTimedGroupsTasks.remove(identifier);
				}
			}, newDelay, TimeUnit.SECONDS));
		}
	}

	@Override
	public boolean has(Player player, String permission) {
		return has(player.getUniqueId(), permission, player.getWorld().getName());
	}

	@Override
	public boolean has(Player player, String permission, String world) {
		return has(player.getUniqueId(), permission, world);
	}

	/**
	 * Check if player with name has permission in world
	 *
	 * @param playerName player name
	 * @param permission permission as string to check against
	 * @param world      world's name as string
	 * @return true on success false otherwise
	 */
	@Override
	public boolean has(String playerName, String permission, String world) {
		PermissionUser user = this.getUser(playerName);

		if (user == null) {
			return false;
		}

		return user.has(permission, world);
	}

	/**
	 * Check if player with UUID has permission in world
	 *
	 * @param playerId player name
	 * @param permission permission as string to check against
	 * @param world      world's name as string
	 * @return true on success false otherwise
	 */
	@Override
	public boolean has(UUID playerId, String permission, String world) {
		PermissionUser user = this.getUser(playerId);

		return user != null && user.has(permission, world);

	}

	/**
	 * Return user's object
	 *
	 * @param username get PermissionUser with given name
	 * @return PermissionUser instance
	 */
	public PermissionUser getUser(String username) {
		if (username == null || username.isEmpty()) {
			throw new IllegalArgumentException("Null or empty name passed! Name must not be empty");
		}

		try {
			if (username.length() != 36) { // Speedup for things def not uuids
				throw new IllegalArgumentException("not a uuid, try stuff");
			}
			return getUser(UUID.fromString(username)); // Username is uuid as string, just use it
		} catch (IllegalArgumentException ex) {
			UUID userUUID = platform.nameToUuid(username);
			boolean online = userUUID != null && platform.isOnline(userUUID);

			if (userUUID != null && (platform.isOnline(userUUID) || backend.hasUser(userUUID.toString()))) {
				return getUser(userUUID.toString(), username, online);
			} else {
				// The user is offline and unconverted, so we'll just have to return an unconverted user.
				return getUser(username, null, false);
			}
		}
	}


	/**
	 * Update a user in cache. This method is thread-safe and should only be called in async phases of login.
	 *
	 * @param ident The user identifier
	 * @param fallbackName Fallback name for user
	 */
	public void cacheUser(String ident, String fallbackName) {
		getUser(ident, fallbackName, true);
	}

	@Override
	public PermissionUser getUser(Player player) {
		return getUser(player.getUniqueId());
	}

	@Override
	public PermissionUser getUser(UUID uid) {
		final String identifier = uid.toString();
		if (users.containsKey(identifier)) {
			return getUser(identifier, null, false);
		}
		String fallbackName = platform.uuidToName(uid);
		return getUser(identifier, fallbackName, fallbackName != null);
	}

	private PermissionUser getUser(String identifier, String fallbackName, boolean store) {
		PermissionUser user = users.get(identifier);

		if (user != null) {
			return user;
		}

		PermissionsUserData data = backend.getUserData(identifier);
		if (data != null) {
			if (fallbackName != null) {
				if (data.isVirtual() && backend.hasUser(fallbackName)) {
					if (isDebug()) {
						getLogger().info("Converting user " + fallbackName + " (UUID " + identifier + ") to UUID-based storage");
					}

					PermissionsUserData oldData = backend.getUserData(fallbackName);
					if (oldData.setIdentifier(identifier)) {
						data = oldData;
						data.setOption("name", fallbackName, null);
						resetUser(fallbackName); // In case somebody requested the old user but conversion was previously unsuccessful
					} else {
						throw new IllegalStateException("User already exists with new id " + identifier + " (converting from " + fallbackName + ")");
					}
				}
			}
			user = new DefaultPermissionUser(identifier, data, this);
			user.initialize();
			if (store) {
				PermissionUser newUser = this.users.put(identifier, user);
				if (newUser != null) {
					user = newUser;
				}
			}
		} else {
			throw new IllegalStateException("User " + identifier + " is null");
		}

		return user;
	}

	/**
	 * Return all registered user objects
	 *
	 * @return unmodifiable list of users
	 */
	public Set<PermissionUser> getUsers() {
		Set<PermissionUser> users = new HashSet<>();
		for (String name : backend.getUserIdentifiers()) {
			users.add(getUser(name, null, false));
		}
		return Collections.unmodifiableSet(users);
	}

	/**
	 * Return users currently cached in memory
	 *
	 * @return A copy of the list of users cached in memory
	 */
	public Set<PermissionUser> getActiveUsers() {
		return new HashSet<>(users.values());
	}

	public Collection<String> getUserIdentifiers() {
		return backend.getUserIdentifiers();
	}

	public Collection<String> getUserNames() {
		return backend.getUserNames();
	}

	@Override
	public Set<PermissionUser> getActiveUsers(String groupName, boolean inheritance) {
		Set<PermissionUser> users = new HashSet<>();

		for (PermissionUser user : this.getActiveUsers()) {
			if (user.inGroup(groupName, inheritance)) {
				users.add(user);
			}
		}

		return Collections.unmodifiableSet(users);
	}

	@Override
	public Set<PermissionUser> getActiveUsers(String groupName) {
		return getActiveUsers(groupName, false);
	}
	/**
	 * Return all users in group
	 *
	 * @param groupName group's name
	 * @return PermissionUser array
	 */
	public Set<PermissionUser> getUsers(String groupName, String worldName) {
		return getUsers(groupName, worldName, false);
	}

	public Set<PermissionUser> getUsers(String groupName) {
		return getUsers(groupName, false);
	}

	/**
	 * Return all users in group and descendant groups
	 *
	 * @param groupName   group's name
	 * @param inheritance true return members of descendant groups of specified group
	 * @return PermissionUser array for groupnName
	 */
	public Set<PermissionUser> getUsers(String groupName, String worldName, boolean inheritance) {
		return groupMembershipIndex.resolveUsers(this, groupName, worldName, inheritance);
	}

	public Set<PermissionUser> getUsers(String groupName, boolean inheritance) {
		return groupMembershipIndex.resolveUsers(this, groupName, null, inheritance);
	}

	void onUserGroupMembershipChanged(PermissionUser user, String world) {
		groupMembershipIndex.onUserMembershipChanged(user, world);
	}

	void onUserRemovedFromIndex(String userId) {
		groupMembershipIndex.untrackUser(userId);
	}

	/**
	 * Reset in-memory object of specified user
	 *
	 * @param userName user's name
	 */
	@Override
	public void resetUser(Player player) {
		resetUser(player.getUniqueId().toString());
	}

	@Override
	public void resetUser(String userName) {
		this.users.remove(userName.toLowerCase());
	}

	/**
	 * Clear cache for specified user
	 *
	 * @param userName
	 */
	public void clearUserCache(String userName) {
		PermissionUser user = this.getUser(userName);

		if (user != null) {
			user.clearCache();
		}
	}

	@Override
	public void clearUserCache(Player player) {
		clearUserCache(player.getUniqueId());
	}

	@Override
	public void clearUserCache(UUID uid) {
		PermissionUser user = this.getUser(uid);

		if (user != null) {
			user.clearCache();
		}
	}

	public PermissionGroup getDefaultGroup() {
		return getDefaultGroup(null);
	}

	/** When several defaults exist (per-world), returns the first in {@link #getDefaultGroups(String)} order. */
	public PermissionGroup getDefaultGroup(String worldName) {
		List<PermissionGroup> defs = getDefaultGroups(worldName);
		if (defs.isEmpty()) {
			return null;
		}
		return defs.get(0);
	}

	/**
	 * Return object for specified group
	 *
	 * @param groupname group's name
	 * @return PermissionGroup object
	 */
	public PermissionGroup getGroup(String groupname) {
		if (groupname == null || groupname.isEmpty()) {
			return null;
		}

		PermissionGroup group = groups.get(groupname.toLowerCase());

		if (group == null) {
			PermissionsGroupData data = this.backend.getGroupData(groupname);
			if (data != null) {
				group = new DefaultPermissionGroup(groupname, data, this);
				PermissionGroup oldGroup;
				if ((oldGroup = this.groups.putIfAbsent(groupname.toLowerCase(), group)) != null) {
					return oldGroup;
				}
				try {
					group.initialize();
				} catch (Exception e) {
					this.groups.remove(groupname.toLowerCase());
					throw new IllegalStateException("Error initializing group " + groupname, e);
				}
			} else {
				throw new IllegalStateException("Group " + groupname + " is null");
			}
		}

		return group;
	}

	/**
	 * Return all groups
	 *
	 * @return PermissionGroup array
	 */
	public List<PermissionGroup> getGroupList() {
		List<PermissionGroup> ret = new LinkedList<>();
		for (String name : backend.getGroupNames()) {
			ret.add(getGroup(name));
		}
		return Collections.unmodifiableList(ret);
	}

	@Deprecated
	public PermissionGroup[] getGroups() {
		return getGroupList().toArray(new PermissionGroup[0]);
	}

	@Override
	@Deprecated
	public Collection<String> getGroupNames() {
		return backend.getGroupNames();
	}

	/**
	 * Return all child groups of specified group
	 *
	 * @param groupName group's name
	 * @return PermissionGroup array
	 */
	public List<PermissionGroup> getGroups(String groupName, String worldName) {
		return getGroups(groupName, worldName, false);
	}

	public List<PermissionGroup> getGroups(String groupName) {
		return getGroups(groupName, null);
	}

	/**
	 * Return all descendants or child groups for groupName
	 *
	 * @param groupName   group's name
	 * @param inheritance true: only direct child groups would be returned
	 * @return unmodifiable PermissionGroup list for specified groupName
	 */
	public List<PermissionGroup> getGroups(String groupName, String worldName, boolean inheritance) {
		List<PermissionGroup> groups = new LinkedList<>();

		for (PermissionGroup group : this.getGroupList()) {
			if (!groups.contains(group) && group.isChildOf(groupName, worldName, inheritance)) {
				groups.add(group);
			}
		}

		return Collections.unmodifiableList(groups);
	}

	public List<PermissionGroup> getGroups(String groupName, boolean inheritance) {
		List<PermissionGroup> groups = new ArrayList<>();

		for (String worldName : platform.realmNames()) {
			groups.addAll(getGroups(groupName, worldName, inheritance));
		}

		// Common space users
		groups.addAll(getGroups(groupName, null, inheritance));

		Collections.sort(groups);

		return Collections.unmodifiableList(groups);
	}

	/**
	 * Return all known default groups
	 *
	 * @param worldName World to check (will include global scope)
	 * @return All default groups
	 */
	public List<PermissionGroup> getDefaultGroups(String worldName) {
		List<PermissionGroup> defaults = new LinkedList<>();
		for (PermissionGroup grp : getGroupList()) {
			if (grp.isDefault(worldName) || (worldName != null && grp.isDefault(null))) {
				defaults.add(grp);
			}
		}

		return Collections.unmodifiableList(defaults);
	}

	/**
	 * Reset in-memory object for groupName
	 *
	 * @param groupName group's name
	 */
	public PermissionGroup resetGroup(String groupName) {
		return this.groups.remove(groupName.toLowerCase());
	}

	void preloadGroups() {
		for (PermissionGroup group : getGroupList()) {
			((AbstractPermissionEntity) group).getData().load();
		}
	}

	/**
	 * Set debug mode
	 *
	 * @param debug true enables debug mode, false disables
	 */
	public void setDebug(boolean debug) {
		this.debugMode = debug;
		this.publishSystem(SystemMutation.DEBUGMODE_TOGGLE);
	}

	/**
	 * Return current state of debug mode
	 *
	 * @return true debug is enabled, false if disabled
	 */
	public boolean isDebug() {
		return debugMode;
	}

	/**
	 * Return groups of specified rank ladder
	 *
	 * @param ladderName
	 * @return Map of ladder, key - rank of group, value - group object. Empty map if ladder does not exist
	 */
	public Map<Integer, PermissionGroup> getRankLadder(String ladderName) {
		Map<Integer, PermissionGroup> ladder = new HashMap<>();

		for (PermissionGroup group : this.getGroupList()) {
			if (!group.isRanked()) {
				continue;
			}

			if (group.getRankLadder().equalsIgnoreCase(ladderName)) {
				ladder.put(group.getRank(), group);
			}
		}

		return ladder;
	}

	/**
	 * Return array of world names who has world inheritance
	 *
	 * @param worldName World name
	 * @return Array of parent world, if world does not exist return empty array
	 */
	public List<String> getWorldInheritance(String worldName) {
		return backend.getWorldInheritance(worldName);
	}

	/**
	 * Set world inheritance parents for world
	 *
	 * @param world        world name which inheritance should be set
	 * @param parentWorlds array of parent world names
	 */
	public void setWorldInheritance(String world, List<String> parentWorlds) {
		backend.setWorldInheritance(world, parentWorlds);
		for (PermissionUser user : getActiveUsers()) { // Clear user cache
			user.clearCache();
		}
		this.publishSystem(SystemMutation.WORLDINHERITANCE_CHANGED);
	}

	/**
	 * Return current backend
	 *
	 * @return current backend object
	 */
	public PermissionBackend getBackend() {
		return this.backend;
	}

	@Override
	public int registeredUserNameCount() {
		return getUserNames().size();
	}

	@Override
	public int registeredGroupCount() {
		return getGroupNames().size();
	}

	@Override
	public String activeBackendSimpleName() {
		PermissionBackend b = this.backend;
		return b != null ? b.getClass().getSimpleName() : "?";
	}

	/**
	 * Set backend to specified backend.
	 * This would also cause backend resetting.
	 *
	 * @param backendName name of backend to set to
	 */
	public void setBackend(String backendName) throws PermissionBackendException {
		synchronized (this) {
			this.clearCache();
			String previous = this.config.getDefaultBackend();
			this.backend = createBackend(backendName);
			if (!backendName.equals(previous)) {
				this.config.setDefaultBackend(backendName);
			}
			this.preloadGroups();
		}

		this.publishSystem(SystemMutation.BACKEND_CHANGED);
	}

	/**
	 * Creates a backend but does not set it as the active backend. Useful for data transfer &amp; such
	 * @param backendName Name of the configuration section which describes this backend
	 */
	public PermissionBackend createBackend(String backendName) throws PermissionBackendException {
		PEXBackendConfiguration backendSection = this.config.pexBackendConfiguration(backendName);
		String backendType = backendSection.getString("type");
		if (backendType == null) {
			backendSection.set("type", backendType = backendName);
		}

		return PermissionBackend.getBackend(backendType, this, backendSection);
	}

	/**
	 * Register new timer task
	 *
	 * @param task  TimerTask object
	 * @param delay delay in seconds
	 */
	@Override
	public void registerTask(TimerTask task, int delay) {
		if (executor == null || delay == PermissionManager.TRANSIENT_PERMISSION) {
			return;
		}

		executor.schedule(task, delay, TimeUnit.SECONDS);
	}

	/**
	 * Reset all in-memory groups and users, clean up runtime stuff, reloads backend
	 */
	public void reset() throws PermissionBackendException {
		reset(true);
	}

	/**
	 * Reset all in-memory groups and users, clean up runtime stuff, reloads backend
	 *
	 * @param callEvent Call the reload event
	 */
	public void reset(boolean callEvent) throws PermissionBackendException {
		this.clearCache();

		if (this.backend != null) {
			this.backend.reload();
		}
		if (callEvent) this.publishSystem(SystemMutation.RELOADED);
	}

	public void end() {
		try {
			if (this.backend != null) {
				this.backend.close();
				this.backend = null;
			}
			reset();
		} catch (PermissionBackendException ignore) {
			// Ignore because we're shutting down so who cares
		}
		executor.shutdown();
		executor = null;
	}

	public void initTimer() {
		if (executor != null) {
			executor.shutdown();
		}

		executor = Executors.newSingleThreadScheduledExecutor();
	}

	protected void clearCache() {
		this.users.clear();
		this.groups.clear();
		groupMembershipIndex.markDirty();

		// Close old timed Permission Timer
		this.initTimer();
	}

	private void initBackend() throws PermissionBackendException {
		this.setBackend(config.getDefaultBackend());
	}

	protected void publishSystem(SystemMutation mutation) {
		platform.publish(new SystemDispatch(getServerUUID(), mutation));
	}

	@Override
	public void publishEntity(String entityIdentifier, String entityType, EntityMutation mutation) {
		platform.publish(new EntityDispatch(getServerUUID(), entityIdentifier, entityType, mutation));
	}

	public PermissionMatcher getPermissionMatcher() {
		return matcher;
	}

	public void setPermissionMatcher(PermissionMatcher matcher) {
		this.matcher = matcher;
	}

	public java.util.Collection<String> getWorldNames() {
		return platform.realmNames();
	}

	public Logger getLogger() {
		return logger;
	}

	public ScheduledExecutorService getExecutor() {
		return executor;
	}

	public boolean shouldSaveDefaultGroup() {
		return config.saveDefaultGroup();
	}
}
