package ru.tehkode.permissions;

/**
 * Mutable data handle for a single permission user.
 *
 * <p>Extends {@link PermissionsData} with user-specific operations. This interface is intentionally
 * minimal; user-only features (such as identifier changes) are kept separate from group data because
 * of the simpler inheritance structure for users.</p>
 *
 * @see PermissionsData
 * @see PermissionsGroupData
 */
public interface PermissionsUserData extends PermissionsData {
	/**
	 * Changes the identifier of this user.
	 *
	 * <p>If another user already exists with {@code identifier}, the operation is rejected and this
	 * handle is unchanged. Only supported for users because of their simpler inheritance structure
	 * compared to groups.</p>
	 *
	 * @param identifier new user identifier (name or UUID string, depending on backend mode)
	 * @return {@code true} if the identifier was updated; {@code false} if a conflict prevented the change
	 */
	public boolean setIdentifier(String identifier);
}
