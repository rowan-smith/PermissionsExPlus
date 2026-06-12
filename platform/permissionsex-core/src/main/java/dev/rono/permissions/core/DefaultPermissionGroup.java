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
import dev.rono.permissions.core.InternalPermissionManager;

import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.PermissionsGroupData;
import java.util.*;

/**
 * @author t3hk0d3
 */
public class DefaultPermissionGroup extends AbstractPermissionEntity implements PermissionGroup, Comparable<PermissionGroup> {
	protected int weight = 0;
	protected boolean dirtyWeight = true;
	private final PermissionsGroupData data;

	public DefaultPermissionGroup(String groupName, PermissionsGroupData data, PermissionManager manager) {
		super(groupName, manager);
		this.data = data;
	}

	@Override
	protected PermissionsGroupData getData() {
		return data;
	}

	@Override
	public void initialize() {
		super.initialize();

		if (this.isDebug()) {
			manager.getLogger().info("Group " + this.getIdentifier() + " initialized");
		}
	}

	@Override
	public PermissionEntity.Type getType() {
		return PermissionEntity.Type.GROUP;
	}

	public int getWeight() {
		if (this.dirtyWeight) {
			this.weight = this.getOwnOptionInteger("weight", null, 0);
			this.dirtyWeight = false;
		}

		return this.weight;
	}

	public void setWeight(int weight) {
		this.setOption("weight", Integer.toString(weight));

		this.dirtyWeight = true;
		this.callEvent(EntityMutation.WEIGHT_CHANGED);
	}

	/**
	 * Checks if group is participating in ranking system
	 *
	 * @return
	 */
	public boolean isRanked() {
		return (this.getRank() > 0);
	}

	/**
	 * Returns rank in ranking system. 0 if group is not ranked
	 *
	 * @return
	 */
	public int getRank() {
		return this.getOwnOptionInteger("rank", null, 0);
	}

	/**
	 * Set rank for this group
	 *
	 * @param rank Rank for group. Specify 0 to remove group from ranking
	 */
	public void setRank(int rank) {
		if (rank > 0) {
			this.setOption("rank", Integer.toString(rank));
		} else {
			this.setOption("rank", null);
		}

		this.callEvent(EntityMutation.RANK_CHANGED);
	}

	/**
	 * Returns ranking ladder where this group is participating in
	 *
	 * @return Name of rank ladder as String
	 */
	public String getRankLadder() {
		return this.getOwnOption("rank-ladder", null, "default");
	}

	/**
	 * Set rank ladder for this group
	 *
	 * @param rankLadder Name of rank ladder
	 */
	public void setRankLadder(String rankLadder) {
		if (rankLadder.isEmpty() || rankLadder.equals("default")) {
			rankLadder = null;
		}

		this.setOption("rank-ladder", rankLadder);

		this.callEvent(EntityMutation.RANK_CHANGED);
	}

	/**
	 * Check if this group is descendant of specified group
	 *
	 * @param group            group object of parent
	 * @param checkInheritance set to false to check only the direct inheritance
	 * @return true if this group is descendant or direct parent of specified group
	 */
	public boolean isChildOf(PermissionGroup group, String worldName, boolean checkInheritance) {
		return isChildOfWithVisited(group, worldName, checkInheritance ? new HashSet<String>() : null);
	}

	private boolean isChildOfWithVisited(PermissionGroup group, String worldName, Set<String> visitedParents) {
		if (group == null) {
			return false;
		}

		if (visitedParents != null) {
			visitedParents.add(this.getIdentifier());
		}

		for (String parentGroup : getData().getParents(worldName)) {
			if (visitedParents != null && visitedParents.contains(parentGroup)) {
				continue;
			}

			if (group.getIdentifier().equals(parentGroup)) {
				return true;
			}

			if (visitedParents != null && ((DefaultPermissionGroup) manager.getGroup(parentGroup)).isChildOfWithVisited(group, worldName, visitedParents)) {
				return true;
			}
		}

		return false;
	}

	public boolean isChildOf(PermissionGroup group, boolean checkInheritance) {
		for (String worldName : this.getWorlds()) {
			if (this.isChildOf(group, worldName, checkInheritance)) {
				return true;
			}
		}

		return this.isChildOf(group, null, checkInheritance);
	}

	public boolean isChildOf(PermissionGroup group, String worldName) {
		return isChildOf(group, worldName, false);
	}

	public boolean isChildOf(PermissionGroup group) {
		return isChildOf(group, false);
	}

	/**
	 * Check if this group is descendant of specified group
	 *
	 * @param groupName        name of group to check against
	 * @param checkInheritance set to false to check only the direct inheritance
	 * @return
	 */
	public boolean isChildOf(String groupName, String worldName, boolean checkInheritance) {
		return isChildOf(this.manager.getGroup(groupName), worldName, checkInheritance);
	}

	public boolean isChildOf(String groupName, boolean checkInheritance) {
		return isChildOf(this.manager.getGroup(groupName), checkInheritance);
	}

	/**
	 * Check if specified group is direct parent of this group
	 *
	 * @param groupName to check against
	 * @return
	 */
	public boolean isChildOf(String groupName, String worldName) {
		return this.isChildOf(groupName, worldName, false);
	}

	public boolean isChildOf(String groupName) {
		return this.isChildOf(groupName, false);
	}

	/**
	 * Return array of direct child group objects
	 *
	 * @return
	 */
	public List<PermissionGroup> getChildGroups(String worldName) {
		return this.manager.getGroups(this.getIdentifier(), worldName, false);
	}

	public List<PermissionGroup> getChildGroups() {
		return this.manager.getGroups(this.getIdentifier(), false);
	}

	/**
	 * Return array of descendant group objects
	 *
	 * @return
	 */
	public List<PermissionGroup> getDescendantGroups(String worldName) {
		return this.manager.getGroups(this.getIdentifier(), worldName, true);
	}

	public List<PermissionGroup> getDescendantGroups() {
		return this.manager.getGroups(this.getIdentifier(), true);
	}

	/**
	 * Return array of direct members (users) of this group
	 *
	 * @return
	 */
	public Set<PermissionUser> getUsers(String worldName) {
		return this.manager.getUsers(this.getIdentifier(), worldName, false);
	}

	public Set<PermissionUser> getUsers() {
		return this.manager.getUsers(this.getIdentifier());
	}

	public Set<PermissionUser> getActiveUsers() {
		return InternalPermissionManager.require(this.manager).getActiveUsers(this.getIdentifier());
	}

	public Set<PermissionUser> getActiveUsers(boolean inheritance) {
		return InternalPermissionManager.require(this.manager).getActiveUsers(this.getIdentifier(), inheritance);
	}

	public boolean isDefault(String worldName) {
		return getOwnOptionBoolean("default", worldName, false);
	}

	public void setDefault(boolean def, String worldName) {
		setOption("default", String.valueOf(def), worldName);
		callEvent(EntityMutation.DEFAULTGROUP_CHANGED);
	}

	@Override
	public void clearCache() {
		this.dirtyWeight = true;
		for (PermissionUser user : this.getActiveUsers()) {
			user.clearCache();
		}
	}

	@Override
	public final void remove() {
		for (String world : this.getWorlds()) {
			this.clearChildren(world);
		}

		this.clearChildren(null);
		super.remove();
	}

	private void clearChildren(String worldName) {
		for (PermissionGroup group : this.getChildGroups(worldName)) {
			List<PermissionGroup> parentGroups = new LinkedList<>(group.getOwnParents(worldName));
			parentGroups.remove(this);

			group.setParents(parentGroups, worldName);
		}

		for (PermissionUser user : this.getUsers(worldName)) {
			user.removeGroup(this, worldName);
		}
	}

	@Override
	public int compareTo(PermissionGroup o) {
		return this.getWeight() - o.getWeight();
	}

	// -- Compatibility methods

	/**
	 * Returns direct parents names of this group
	 *
	 * @return array of parents group names
	 * @deprecated See {@link #getParentIdentifiers(String)}
	 */
	@Deprecated
	public String[] getParentGroupsNames(String worldName) {
		return getParentIdentifiers(worldName).toArray(new String[0]);
	}

	@Deprecated
	public String[] getParentGroupsNames() {
		return getParentIdentifiers().toArray(new String[0]);
	}

	/**
	 * Set parent groups
	 *
	 * @param parentGroups Array of parent groups names to set
	 * @deprecated See {@link #setParentsIdentifier(List, String)}
	 */
	@Deprecated
	public void setParentGroups(List<String> parentGroups, String worldName) {
		setParentsIdentifier(parentGroups, worldName);
	}

	@Deprecated
	public void setParentGroups(List<String> parentGroups) {
		this.setParentsIdentifier(parentGroups);
	}

	/**
	 * Set parent groups
	 *
	 * @param parentGroups Array of parent groups objects to set
	 */
	@Deprecated
	public void setParentGroupObjects(List<PermissionGroup> parentGroups, String worldName) {
		setParents(parentGroups, worldName);
	}

	@Deprecated
	public void setParentGroupObjects(List<PermissionGroup> parentGroups) {
		this.setParents(parentGroups);
	}


	/**
	 * Returns array of parent groups objects
	 *
	 * @return unmodifiable list of parent group objects
	 * @deprecated Use {@link #getParents(String)} instead
	 */
	@Deprecated
	public List<PermissionGroup> getParentGroups(String worldName) {
		return getParents(worldName);
	}

	@Deprecated
	public List<PermissionGroup> getParentGroups() {
		return this.getParentGroups(null);
	}

	@Deprecated
	public Map<String, List<PermissionGroup>> getAllParentGroups() {
		return getAllParents();
	}

	public void addParent(PermissionGroup parent, String worldName) {
		List<PermissionGroup> next = new LinkedList<>(getOwnParents(worldName));
		boolean present = next.stream().anyMatch(g -> g.getIdentifier().equals(parent.getIdentifier()));
		if (!present) {
			next.add(parent);
			setParents(next, worldName);
		}
	}

	public void addParent(PermissionGroup parent) {
		addParent(parent, null);
	}

	public void addParent(String parentName, String worldName) {
		addParent(manager.getGroup(parentName), worldName);
	}

	public void addParent(String parentName) {
		addParent(parentName, null);
	}

	public void removeParent(PermissionGroup parent, String worldName) {
		List<PermissionGroup> next = new LinkedList<>(getOwnParents(worldName));
		next.removeIf(g -> g.getIdentifier().equalsIgnoreCase(parent.getIdentifier()));
		setParents(next, worldName);
	}

	public void removeParent(PermissionGroup parent) {
		removeParent(parent, null);
	}

	public void removeParent(String parentName, String worldName) {
		removeParent(manager.getGroup(parentName), worldName);
	}

	public void removeParent(String parentName) {
		removeParent(parentName, null);
	}
}
