package ru.tehkode.permissions;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Common read/write contract for permission entity data (users and groups).
 *
 * <p>Implementations represent a single user or group and expose world-scoped permissions, options,
 * and parent-group inheritance. Changes are held in memory until {@link #save()} is called, or may be
 * written immediately depending on backend persistence settings.</p>
 *
 * @see PermissionsUserData
 * @see PermissionsGroupData
 */
public interface PermissionsData {
	/**
	 * Preloads entity data from the backing storage into this handle.
	 *
	 * <p>Subsequent getter calls reflect the loaded state. Implementations may call this lazily on
	 * first access.</p>
	 */
	public void load();

	/**
	 * Returns the current identifier of this entity.
	 *
	 * @return user or group identifier; never {@code null} for persisted entities
	 */
	public String getIdentifier();

	/**
	 * Returns all permission nodes assigned to this entity in the specified world.
	 *
	 * @param worldName world name, or {@code null} for the global (common) context
	 * @return list of permission strings; never {@code null}
	 */
	public List<String> getPermissions(String worldName);

	/**
	 * Replaces all permission nodes for this entity in the specified world.
	 *
	 * @param permissions new permission list; may be empty but not {@code null}
	 * @param worldName   world name, or {@code null} for the global (common) context
	 */
	public void setPermissions(List<String> permissions, String worldName);

	/**
	 * Returns all permission nodes for this entity, keyed by world name.
	 *
	 * @return map from world name to permission lists; never {@code null}
	 */
	public Map<String, List<String>> getPermissionsMap();

	/**
	 * Returns the set of worlds in which this entity has permissions or options defined.
	 *
	 * @return set of world names; never {@code null}
	 */
	public Set<String> getWorlds();

	/**
	 * Returns the value of a named option in the specified world.
	 *
	 * @param option    option key (for example {@code "prefix"} or {@code "suffix"})
	 * @param worldName world name, or {@code null} for the global (common) context
	 * @return option value, or {@code null} if the option is not defined in that world
	 */
	public String getOption(String option, String worldName);

	/**
	 * Sets a named option value in the specified world.
	 *
	 * @param option option key
	 * @param value  option value; may be {@code null} to clear the option
	 * @param world  world name, or {@code null} for the global (common) context
	 */
	public void setOption(String option, String value, String world);

	/**
	 * Returns all options defined for this entity in the specified world.
	 *
	 * @param worldName world name, or {@code null} for the global (common) context
	 * @return map from option key to value; never {@code null}
	 */
	public Map<String, String> getOptions(String worldName);

	/**
	 * Returns all options for this entity, keyed by world name.
	 *
	 * @return map from world name to option maps; never {@code null}
	 */
	public Map<String, Map<String, String>> getOptionsMap();

	/**
	 * Returns the parent groups of this user or group in the specified world.
	 *
	 * @param worldName world name, or {@code null} for the global (common) context
	 * @return unmodifiable list of parent group names; never {@code null}
	 */
	public List<String> getParents(String worldName);

	/**
	 * Sets the parent groups of this user or group in the specified world.
	 *
	 * @param parents   new list of parent group names; may be empty but not {@code null}
	 * @param worldName world name, or {@code null} for the global (common) context
	 */
	public void setParents(List<String> parents, String worldName);

	/**
	 * Returns whether this entity exists only in server memory and has not been persisted.
	 *
	 * @return {@code true} if the entity is virtual (not yet saved to the backend)
	 */
	public boolean isVirtual();

	/**
	 * Persists pending changes for this entity to the backend.
	 */
	public void save();

	/**
	 * Removes this entity and all associated data from the backend.
	 */
	public void remove();

	/**
	 * Returns parent group mappings for all worlds.
	 *
	 * @return map from world name to parent group lists; never {@code null}
	 */
	public Map<String,List<String>> getParentsMap();
}
