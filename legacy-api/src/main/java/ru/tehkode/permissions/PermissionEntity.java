package ru.tehkode.permissions;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Classic permission subject (user/group) contract ({@code ru.tehkode.permissions}).
 *
 * <p>Concrete implementations ship in {@code permissionsex-core} ({@code AbstractPermissionEntity}).
 */
public interface PermissionEntity {

	String NON_INHERITABLE_PREFIX = "#";

	enum Type {
		USER,
		GROUP
	}

	void clearCache();

	void initialize();

	PermissionManager getPermissionManager();

	String getIdentifier();
	String getName();

	PermissionEntity.Type getType();

	String getOwnPrefix();
	String getOwnPrefix(String worldName);
	String getOwnSuffix();
	String getOwnSuffix(String worldName);

	String getPrefix();

	String getPrefix(String worldName);
	void setPrefix(String prefix, String worldName);
	String getSuffix(String worldName);
	String getSuffix();
	void setSuffix(String suffix, String worldName);
	boolean has(String permission);
	boolean has(String permission, String world);
	List<String> getPermissions(String world);
	List<String> getPermissions();

	List<String> getOwnPermissions(String world);

	Map<String, List<String>> getAllPermissions();
	void addPermission(String permission, String worldName);
	void addPermission(String permission);
	void removePermission(String permission, String worldName);
	void removePermission(String permission);
	void setPermissions(List<String> permissions, String world);
	void setPermissions(List<String> permission);
	String getOption(String option, String world, String defaultValue);
	String getOption(String option);
	String getOption(String option, String world);
	int getOptionInteger(String optionName, String world, int defaultValue);
	double getOptionDouble(String optionName, String world, double defaultValue);
	boolean getOptionBoolean(String optionName, String world, boolean defaultValue);
	void setOption(String option, String value, String world);
	void setOption(String option, String value);
	Map<String, String> getOptions(String world);
	Map<String, String> getOptions();
	Map<String, Map<String, String>> getAllOptions();
	String getOwnOption(String option, String world, String defaultValue);
	String getOwnOption(String option);
	String getOwnOption(String option, String world);
	int getOwnOptionInteger(String optionName, String world, int defaultValue);
	boolean getOwnOptionBoolean(String optionName, String world, boolean defaultValue);
	double getOwnOptionDouble(String optionName, String world, double defaultValue);
	void save();
	void remove();
	boolean isVirtual();
	Set<String> getWorlds();
	List<String> getTimedPermissions(String world);
	int getTimedPermissionLifetime(String permission, String world);
	void addTimedPermission(String permission, String world, int lifeTime);
	void removeTimedPermission(String permission, String world);
	String getMatchingExpression(String permission, String world);
	String getMatchingExpression(List<String> permissions, String permission);
	boolean isMatches(String expression, String permission, boolean additionalChecks);
	boolean explainExpression(String expression);
	boolean isDebug();
	void setDebug(boolean debug);
	List<PermissionGroup> getOwnParents(String world);
	List<PermissionGroup> getOwnParents();
	List<String> getOwnParentIdentifiers(String world);
	List<String> getOwnParentIdentifiers();
	List<PermissionGroup> getParents(String world);
	List<PermissionGroup> getParents();
	List<String> getParentIdentifiers(String world);
	List<String> getParentIdentifiers();
	Map<String, List<PermissionGroup>> getAllParents();
	void setParents(List<PermissionGroup> parents, String world);
	void setParents(List<PermissionGroup> parents);
	void setParentsIdentifier(List<String> parentNames, String world);
	void setParentsIdentifier(List<String> parentNames);

}
