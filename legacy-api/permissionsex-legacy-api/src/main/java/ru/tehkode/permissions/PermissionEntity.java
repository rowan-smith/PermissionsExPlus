package ru.tehkode.permissions;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base contract for permission subjects (users and groups) in the classic {@code ru.tehkode.permissions} API.
 *
 * <p>Concrete implementations ship in {@code permissionsex-core} ({@code AbstractPermissionEntity},
 * {@link PermissionUser}, {@link PermissionGroup}).</p>
 *
 * <p><strong>Frozen public contract.</strong> Baseline commit {@code 628215f}. Do not extend this
 * interface with new methods — use the modern API for new features.</p>
 *
 * <h2>World scope</h2>
 * <p>A {@code null} world name denotes the <em>global</em> (common) scope: permissions, options,
 * prefix/suffix, and parent groups assigned for all worlds unless overridden by world-specific data.</p>
 *
 * <h2>Direct ({@code getOwn*}) vs effective ({@code get*})</h2>
 * <p>Methods prefixed with {@code getOwn} return values stored directly on this entity only.
 * Methods without {@code Own} resolve <em>effective</em> values by traversing parent groups and
 * world inheritance (see {@link PermissionManager#getWorldInheritance(String)}).</p>
 *
 * <h2>Non-inheritable permissions</h2>
 * <p>Permissions prefixed with {@link #NON_INHERITABLE_PREFIX} ({@code "#"}) are not propagated
 * to child subjects through inheritance.</p>
 *
 * <h2>Timed permissions</h2>
 * <p>Temporary grants are tracked separately from persisted permissions. A lifetime of
 * {@link PermissionManager#TRANSIENT_PERMISSION} ({@code 0}) means the grant is held in memory only
 * and is not written to the backend.</p>
 *
 * @see PermissionUser
 * @see PermissionGroup
 * @see PermissionManager
 */
public interface PermissionEntity {

	/**
	 * Prefix ({@code "#"}) marking a permission expression as non-inheritable by child groups or users.
	 *
	 * <p>When a permission starts with this character, descendants do not receive it through
	 * group inheritance (see {@link #getPermissions(String)} vs {@link #getOwnPermissions(String)}).</p>
	 */
	String NON_INHERITABLE_PREFIX = "#";

	/**
	 * Kind of permission subject represented by this entity.
	 */
	enum Type {

		/** An online player or offline user record. */
		USER,

		/** A named permission group. */
		GROUP
	}

	/**
	 * Clears in-memory caches for this entity (resolved permissions, inheritance, and related derived state).
	 *
	 * <p>Implementations invoke this after mutating permissions, options, or parent groups so subsequent
	 * {@code get*} calls recompute effective values.</p>
	 */
	void clearCache();

	/**
	 * Completes post-construction initialization for this entity.
	 *
	 * <p>Called by the owning {@link PermissionManager} after the entity and its backing data are fully
	 * constructed. Typically loads debug flags and other derived state from stored options.</p>
	 */
	void initialize();

	/**
	 * Returns the {@link PermissionManager} that owns and resolves this entity.
	 *
	 * @return the permission manager instance
	 */
	PermissionManager getPermissionManager();

	/**
	 * Returns the stable backend identifier for this entity (UUID string for users, group name for groups).
	 *
	 * @return entity identifier; never {@code null} for a live instance
	 */
	String getIdentifier();

	/**
	 * Returns the display name for this entity.
	 *
	 * <p>May differ from {@link #getIdentifier()} when a {@code name} option is set; otherwise falls back
	 * to the identifier.</p>
	 *
	 * @return display name
	 */
	String getName();

	/**
	 * Returns whether this entity represents a user or a group.
	 *
	 * @return {@link Type#USER} or {@link Type#GROUP}
	 */
	PermissionEntity.Type getType();

	/**
	 * Returns the prefix stored directly on this entity in the global scope.
	 *
	 * <p>Does not traverse parent groups. Returns {@code null} or empty when no own prefix is set.</p>
	 *
	 * @return direct prefix, or {@code null}/empty if unset
	 * @see #getPrefix()
	 */
	String getOwnPrefix();

	/**
	 * Returns the prefix stored directly on this entity for the given world.
	 *
	 * <p>{@code worldName == null} selects the global scope. Does not traverse parent groups.</p>
	 *
	 * @param worldName world name, or {@code null} for global scope
	 * @return direct prefix, or {@code null}/empty if unset
	 * @see #getPrefix(String)
	 */
	String getOwnPrefix(String worldName);

	/**
	 * Returns the suffix stored directly on this entity in the global scope.
	 *
	 * <p>Does not traverse parent groups. Returns {@code null} or empty when no own suffix is set.</p>
	 *
	 * @return direct suffix, or {@code null}/empty if unset
	 * @see #getSuffix()
	 */
	String getOwnSuffix();

	/**
	 * Returns the suffix stored directly on this entity for the given world.
	 *
	 * <p>{@code worldName == null} selects the global scope. Does not traverse parent groups.</p>
	 *
	 * @param worldName world name, or {@code null} for global scope
	 * @return direct suffix, or {@code null}/empty if unset
	 * @see #getSuffix(String)
	 */
	String getOwnSuffix(String worldName);

	/**
	 * Returns the effective chat prefix in the global scope, including inheritance from parent groups.
	 *
	 * @return resolved prefix, or empty string when none is defined
	 * @see #getOwnPrefix()
	 */
	String getPrefix();

	/**
	 * Returns the effective chat prefix for the given world, including inheritance from parent groups
	 * and world inheritance.
	 *
	 * <p>{@code worldName == null} selects the global scope.</p>
	 *
	 * @param worldName world name, or {@code null} for global scope
	 * @return resolved prefix, or empty string when none is defined
	 * @see #getOwnPrefix(String)
	 */
	String getPrefix(String worldName);

	/**
	 * Sets the chat prefix stored directly on this entity for the given world.
	 *
	 * <p>{@code worldName == null} stores the prefix in global scope. Clears entity cache after update.</p>
	 *
	 * @param prefix    new prefix value
	 * @param worldName world name, or {@code null} for global scope
	 */
	void setPrefix(String prefix, String worldName);

	/**
	 * Returns the effective chat suffix for the given world, including inheritance from parent groups
	 * and world inheritance.
	 *
	 * <p>{@code worldName == null} selects the global scope.</p>
	 *
	 * @param worldName world name, or {@code null} for global scope
	 * @return resolved suffix, or empty string when none is defined
	 * @see #getOwnSuffix(String)
	 */
	String getSuffix(String worldName);

	/**
	 * Returns the effective chat suffix in the global scope, including inheritance from parent groups.
	 *
	 * @return resolved suffix, or empty string when none is defined
	 * @see #getOwnSuffix()
	 */
	String getSuffix();

	/**
	 * Sets the chat suffix stored directly on this entity for the given world.
	 *
	 * <p>{@code worldName == null} stores the suffix in global scope. Clears entity cache after update.</p>
	 *
	 * @param suffix    new suffix value
	 * @param worldName world name, or {@code null} for global scope
	 */
	void setSuffix(String suffix, String worldName);

	/**
	 * Checks whether this entity effectively holds the given permission in the default world context.
	 *
	 * <p>Equivalent to {@link #has(String, String)} with a server-default world name when worlds exist,
	 * or global scope ({@code null}) otherwise.</p>
	 *
	 * @param permission permission node to check
	 * @return {@code true} if the permission is granted, {@code false} otherwise
	 */
	boolean has(String permission);

	/**
	 * Checks whether this entity effectively holds the given permission in the specified world.
	 *
	 * <p>Resolves inheritance, timed permissions, negated nodes ({@code -node}), and
	 * {@link #NON_INHERITABLE_PREFIX non-inheritable} expressions. {@code world == null} uses global scope.</p>
	 *
	 * @param permission permission node to check
	 * @param world      world name, or {@code null} for global scope
	 * @return {@code true} if the permission is granted, {@code false} otherwise
	 */
	boolean has(String permission, String world);

	/**
	 * Returns the effective permission expressions for the given world, including inherited and timed grants.
	 *
	 * <p>{@code world == null} selects global scope. Order reflects precedence for matching
	 * (see {@link #getMatchingExpression(String, String)}).</p>
	 *
	 * @param world world name, or {@code null} for global scope
	 * @return unmodifiable list of effective permission expressions
	 * @see #getOwnPermissions(String)
	 */
	List<String> getPermissions(String world);

	/**
	 * Returns effective permission expressions in the global scope.
	 *
	 * @return unmodifiable list of effective permission expressions
	 * @see #getPermissions(String)
	 */
	List<String> getPermissions();

	/**
	 * Returns permission expressions assigned directly to this entity for the given world.
	 *
	 * <p>Does not include inherited parent-group permissions or timed grants. {@code world == null}
	 * selects global scope.</p>
	 *
	 * @param world world name, or {@code null} for global scope
	 * @return unmodifiable list of directly assigned permission expressions
	 * @see #getPermissions(String)
	 */
	List<String> getOwnPermissions(String world);

	/**
	 * Returns all permission lists keyed by world name.
	 *
	 * <p>Global-scope permissions are stored under a {@code null} key. Timed permissions for each world
	 * are merged into the corresponding entry.</p>
	 *
	 * @return unmodifiable map of world name to permission expression list
	 */
	Map<String, List<String>> getAllPermissions();

	/**
	 * Adds a permission expression to this entity for the given world, placing it at highest precedence.
	 *
	 * <p>If the expression already exists for that world, it is moved to the front of the list.
	 * {@code worldName == null} selects global scope.</p>
	 *
	 * @param permission permission expression to add
	 * @param worldName  world name, or {@code null} for global scope
	 */
	void addPermission(String permission, String worldName);

	/**
	 * Adds a permission expression in global scope ({@code worldName == null}).
	 *
	 * @param permission permission expression to add
	 * @see #addPermission(String, String)
	 */
	void addPermission(String permission);

	/**
	 * Removes a permission expression (and any matching timed grant) from this entity for the given world.
	 *
	 * @param permission permission expression to remove
	 * @param worldName  world name, or {@code null} for global scope
	 */
	void removePermission(String permission, String worldName);

	/**
	 * Removes the given permission expression from every world where this entity defines permissions.
	 *
	 * @param permission permission expression to remove
	 */
	void removePermission(String permission);

	/**
	 * Replaces the direct permission list for the given world.
	 *
	 * <p>{@code world == null} selects global scope. Clears entity cache after update.</p>
	 *
	 * @param permissions new permission expression list
	 * @param world       world name, or {@code null} for global scope
	 */
	void setPermissions(List<String> permissions, String world);

	/**
	 * Replaces the direct permission list in global scope.
	 *
	 * @param permission new permission expression list
	 * @see #setPermissions(List, String)
	 */
	void setPermissions(List<String> permission);

	/**
	 * Returns the effective string value of an option, traversing parent groups when not set locally.
	 *
	 * @param option       option name
	 * @param world        world name, or {@code null} for global scope
	 * @param defaultValue value returned when the option is unset after inheritance resolution
	 * @return resolved option value, or {@code defaultValue} if absent
	 * @see #getOwnOption(String, String, String)
	 */
	String getOption(String option, String world, String defaultValue);

	/**
	 * Returns the effective value of an option in global scope.
	 *
	 * @param option option name
	 * @return resolved option value, or {@code null} if unset
	 * @see #getOption(String, String, String)
	 */
	String getOption(String option);

	/**
	 * Returns the effective value of an option for the given world.
	 *
	 * @param option option name
	 * @param world  world name, or {@code null} for global scope
	 * @return resolved option value, or {@code null} if unset
	 * @see #getOption(String, String, String)
	 */
	String getOption(String option, String world);

	/**
	 * Returns the effective integer value of an option for the given world.
	 *
	 * @param optionName   option name
	 * @param world        world name, or {@code null} for global scope
	 * @param defaultValue value returned when the option is unset or not parseable as an integer
	 * @return parsed integer option value, or {@code defaultValue}
	 */
	int getOptionInteger(String optionName, String world, int defaultValue);

	/**
	 * Returns the effective double value of an option for the given world.
	 *
	 * @param optionName   option name
	 * @param world        world name, or {@code null} for global scope
	 * @param defaultValue value returned when the option is unset or not parseable as a double
	 * @return parsed double option value, or {@code defaultValue}
	 */
	double getOptionDouble(String optionName, String world, double defaultValue);

	/**
	 * Returns the effective boolean value of an option for the given world.
	 *
	 * @param optionName   option name
	 * @param world        world name, or {@code null} for global scope
	 * @param defaultValue value returned when the option is unset or not {@code "true"}/{@code "false"}
	 * @return parsed boolean option value, or {@code defaultValue}
	 */
	boolean getOptionBoolean(String optionName, String world, boolean defaultValue);

	/**
	 * Sets an option value stored directly on this entity for the given world.
	 *
	 * <p>Pass {@code null} as {@code value} to remove the option. {@code world == null} selects global scope.</p>
	 *
	 * @param option option name
	 * @param value  option value, or {@code null} to remove
	 * @param world  world name, or {@code null} for global scope
	 */
	void setOption(String option, String value, String world);

	/**
	 * Sets an option value in global scope.
	 *
	 * @param option option name
	 * @param value  option value, or {@code null} to remove
	 * @see #setOption(String, String, String)
	 */
	void setOption(String option, String value);

	/**
	 * Returns all options stored directly on this entity for the given world.
	 *
	 * <p>Does not merge inherited parent options. {@code world == null} selects global scope.</p>
	 *
	 * @param world world name, or {@code null} for global scope
	 * @return map of option name to value
	 */
	Map<String, String> getOptions(String world);

	/**
	 * Returns all options stored directly on this entity in global scope.
	 *
	 * @return map of option name to value
	 * @see #getOptions(String)
	 */
	Map<String, String> getOptions();

	/**
	 * Returns all directly stored options keyed by world name.
	 *
	 * <p>Global-scope options are stored under a {@code null} key.</p>
	 *
	 * @return map of world name to option map
	 */
	Map<String, Map<String, String>> getAllOptions();

	/**
	 * Returns the value of an option stored directly on this entity for the given world.
	 *
	 * <p>Does not traverse parent groups. {@code world == null} selects global scope.</p>
	 *
	 * @param option       option name
	 * @param world        world name, or {@code null} for global scope
	 * @param defaultValue value returned when the option is not set on this entity
	 * @return direct option value, or {@code defaultValue} if absent
	 * @see #getOption(String, String, String)
	 */
	String getOwnOption(String option, String world, String defaultValue);

	/**
	 * Returns the value of an option stored directly on this entity in global scope.
	 *
	 * @param option option name
	 * @return direct option value, or {@code null} if unset
	 * @see #getOwnOption(String, String, String)
	 */
	String getOwnOption(String option);

	/**
	 * Returns the value of an option stored directly on this entity for the given world.
	 *
	 * @param option option name
	 * @param world  world name, or {@code null} for global scope
	 * @return direct option value, or {@code null} if unset
	 * @see #getOwnOption(String, String, String)
	 */
	String getOwnOption(String option, String world);

	/**
	 * Returns the integer value of an option stored directly on this entity.
	 *
	 * @param optionName   option name
	 * @param world        world name, or {@code null} for global scope
	 * @param defaultValue value returned when unset or not parseable
	 * @return parsed integer, or {@code defaultValue}
	 * @see #getOptionInteger(String, String, int)
	 */
	int getOwnOptionInteger(String optionName, String world, int defaultValue);

	/**
	 * Returns the boolean value of an option stored directly on this entity.
	 *
	 * @param optionName   option name
	 * @param world        world name, or {@code null} for global scope
	 * @param defaultValue value returned when unset or not {@code "true"}/{@code "false"}
	 * @return parsed boolean, or {@code defaultValue}
	 * @see #getOptionBoolean(String, String, boolean)
	 */
	boolean getOwnOptionBoolean(String optionName, String world, boolean defaultValue);

	/**
	 * Returns the double value of an option stored directly on this entity.
	 *
	 * @param optionName   option name
	 * @param world        world name, or {@code null} for global scope
	 * @param defaultValue value returned when unset or not parseable
	 * @return parsed double, or {@code defaultValue}
	 * @see #getOptionDouble(String, String, double)
	 */
	double getOwnOptionDouble(String optionName, String world, double defaultValue);

	/**
	 * Persists in-memory changes for this entity to the active backend.
	 *
	 * <p>Clears entity cache after a successful save.</p>
	 */
	void save();

	/**
	 * Deletes this entity from the backend and clears associated in-memory state.
	 */
	void remove();

	/**
	 * Returns whether this entity exists only in memory and has not yet been persisted.
	 *
	 * @return {@code true} if virtual (not yet saved), {@code false} if backed by storage
	 */
	boolean isVirtual();

	/**
	 * Returns world names for which this entity has stored permissions, options, or parent assignments.
	 *
	 * @return set of world names (may be empty)
	 */
	Set<String> getWorlds();

	/**
	 * Returns timed (temporary) permission expressions active for the given world on this entity.
	 *
	 * <p>{@code world == null} selects global scope. Does not include inherited timed grants from parents.</p>
	 *
	 * @param world world name, or {@code null} for global scope
	 * @return unmodifiable list of timed permission expressions
	 */
	List<String> getTimedPermissions(String world);

	/**
	 * Returns remaining lifetime in seconds for a timed permission on this entity.
	 *
	 * <p>{@code world == null} selects global scope. Returns {@code 0} when the permission is transient
	 * ({@link PermissionManager#TRANSIENT_PERMISSION}) or not timed.</p>
	 *
	 * @param permission permission expression
	 * @param world      world name, or {@code null} for global scope
	 * @return seconds remaining, or {@code 0} if not timed or already expired
	 */
	int getTimedPermissionLifetime(String permission, String world);

	/**
	 * Grants a timed permission on this entity for the given world.
	 *
	 * <p>{@code world == null} selects global scope. {@code lifeTime} is in seconds; pass
	 * {@link PermissionManager#TRANSIENT_PERMISSION} ({@code 0}) for a non-persisted in-memory grant that
	 * survives until reload.</p>
	 *
	 * @param permission permission expression to grant
	 * @param world      world name, or {@code null} for global scope
	 * @param lifeTime   lifetime in seconds, or {@code 0} for transient
	 */
	void addTimedPermission(String permission, String world, int lifeTime);

	/**
	 * Revokes a timed permission from this entity for the given world.
	 *
	 * @param permission permission expression to revoke
	 * @param world      world name, or {@code null} for global scope
	 */
	void removeTimedPermission(String permission, String world);

	/**
	 * Finds the highest-precedence permission expression that matches the requested node in the given world.
	 *
	 * <p>Uses the entity's effective permission list for {@code world} (see {@link #getPermissions(String)}).</p>
	 *
	 * @param permission permission node to match
	 * @param world      world name, or {@code null} for global scope
	 * @return winning expression, or {@code null} if none matches
	 */
	String getMatchingExpression(String permission, String world);

	/**
	 * Finds the first expression in the supplied list that matches the requested permission node.
	 *
	 * @param permissions ordered list of permission expressions to search
	 * @param permission  permission node to match
	 * @return winning expression, or {@code null} if none matches
	 */
	String getMatchingExpression(List<String> permissions, String permission);

	/**
	 * Tests whether a permission expression matches a concrete permission node.
	 *
	 * @param expression        stored permission expression (may include wildcards or regex)
	 * @param permission        permission node being checked
	 * @param additionalChecks  when {@code true}, enables parent-node matching semantics in the matcher
	 * @return {@code true} if the expression matches the node
	 */
	boolean isMatches(String expression, String permission, boolean additionalChecks);

	/**
	 * Interprets a permission expression as grant or deny.
	 *
	 * <p>Expressions prefixed with {@code -} are negations and yield {@code false}; all other non-empty
	 * expressions yield {@code true}. {@code null} or empty expressions yield {@code false}.</p>
	 *
	 * @param expression permission expression to interpret
	 * @return {@code true} if the expression grants access, {@code false} if denied or absent
	 */
	boolean explainExpression(String expression);

	/**
	 * Returns whether debug logging is enabled for this entity or its manager.
	 *
	 * @return {@code true} if entity-level or manager-level debug is active
	 */
	boolean isDebug();

	/**
	 * Enables or disables debug logging for this entity only.
	 *
	 * <p>Does not change manager-wide debug mode (see {@link PermissionManager#setDebug(boolean)}).</p>
	 *
	 * @param debug {@code true} to enable entity debug output
	 */
	void setDebug(boolean debug);

	/**
	 * Returns parent groups assigned directly to this entity for the given world.
	 *
	 * <p>Does not traverse the group hierarchy. {@code world == null} selects global scope.</p>
	 *
	 * @param world world name, or {@code null} for global scope
	 * @return unmodifiable list of directly assigned parent groups
	 * @see #getParents(String)
	 */
	List<PermissionGroup> getOwnParents(String world);

	/**
	 * Returns parent groups assigned directly to this entity in global scope.
	 *
	 * @return unmodifiable list of directly assigned parent groups
	 * @see #getOwnParents(String)
	 */
	List<PermissionGroup> getOwnParents();

	/**
	 * Returns identifiers of parent groups assigned directly to this entity for the given world.
	 *
	 * @param world world name, or {@code null} for global scope
	 * @return unmodifiable list of parent group identifiers
	 * @see #getParentIdentifiers(String)
	 */
	List<String> getOwnParentIdentifiers(String world);

	/**
	 * Returns identifiers of parent groups assigned directly to this entity in global scope.
	 *
	 * @return unmodifiable list of parent group identifiers
	 * @see #getOwnParentIdentifiers(String)
	 */
	List<String> getOwnParentIdentifiers();

	/**
	 * Returns all parent groups that contribute to this entity's effective inheritance for the given world.
	 *
	 * <p>Includes ancestors reachable through the group hierarchy. {@code world == null} selects global scope.</p>
	 *
	 * @param world world name, or {@code null} for global scope
	 * @return unmodifiable list of resolved parent groups
	 * @see #getOwnParents(String)
	 */
	List<PermissionGroup> getParents(String world);

	/**
	 * Returns all parent groups that contribute to effective inheritance in global scope.
	 *
	 * @return unmodifiable list of resolved parent groups
	 * @see #getParents(String)
	 */
	List<PermissionGroup> getParents();

	/**
	 * Returns identifiers of all parent groups in the effective inheritance chain for the given world.
	 *
	 * @param world world name, or {@code null} for global scope
	 * @return unmodifiable list of parent group identifiers
	 * @see #getOwnParentIdentifiers(String)
	 */
	List<String> getParentIdentifiers(String world);

	/**
	 * Returns identifiers of all parent groups in the effective inheritance chain in global scope.
	 *
	 * @return unmodifiable list of parent group identifiers
	 * @see #getParentIdentifiers(String)
	 */
	List<String> getParentIdentifiers();

	/**
	 * Returns direct parent groups keyed by world name.
	 *
	 * <p>Global-scope parents are stored under a {@code null} key. Values are direct assignments only
	 * (see {@link #getOwnParents(String)}), not fully resolved inheritance chains.</p>
	 *
	 * @return unmodifiable map of world name to parent group list
	 */
	Map<String, List<PermissionGroup>> getAllParents();

	/**
	 * Replaces the direct parent group list for the given world.
	 *
	 * @param parents   new parent groups
	 * @param world     world name, or {@code null} for global scope
	 */
	void setParents(List<PermissionGroup> parents, String world);

	/**
	 * Replaces the direct parent group list in global scope.
	 *
	 * @param parents new parent groups
	 * @see #setParents(List, String)
	 */
	void setParents(List<PermissionGroup> parents);

	/**
	 * Replaces the direct parent group identifiers for the given world.
	 *
	 * @param parentNames parent group identifiers
	 * @param world       world name, or {@code null} for global scope
	 */
	void setParentsIdentifier(List<String> parentNames, String world);

	/**
	 * Replaces the direct parent group identifiers in global scope.
	 *
	 * @param parentNames parent group identifiers
	 * @see #setParentsIdentifier(List, String)
	 */
	void setParentsIdentifier(List<String> parentNames);

}
