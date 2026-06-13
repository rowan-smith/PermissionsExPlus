package ru.tehkode.permissions;

/**
 * Tri-state result of a permission check.
 *
 * <p>Unlike a plain {@code boolean}, this enum distinguishes an explicit grant or denial from an
 * undefined result where no matching permission node was found.</p>
 */
public enum PermissionCheckResult {

	/** No matching permission node was found; the result is neither granted nor denied. */
	UNDEFINED(false),
	/** Permission is explicitly granted. */
	TRUE(true),
	/** Permission is explicitly denied. */
	FALSE(false);

	/** Whether this result represents an explicit grant ({@code TRUE}) or denial/undefined. */
	protected boolean result;

	/**
	 * Creates a check result with the given boolean backing value.
	 *
	 * @param result {@code true} for {@link #TRUE}, {@code false} for {@link #FALSE} and {@link #UNDEFINED}
	 */
	private PermissionCheckResult(boolean result) {
		this.result = result;
	}

	/**
	 * Returns this result as a plain boolean.
	 *
	 * <p>{@link #UNDEFINED} and {@link #FALSE} both return {@code false}; only {@link #TRUE} returns
	 * {@code true}.</p>
	 *
	 * @return {@code true} only when this result is {@link #TRUE}
	 */
	public boolean toBoolean() {
		return this.result;
	}

	/**
	 * Returns a human-readable representation of this result.
	 *
	 * @return {@code "undefined"} for {@link #UNDEFINED}, otherwise {@code "true"} or {@code "false"}
	 */
	@Override
	public String toString() {
		return this == UNDEFINED ? "undefined" : Boolean.toString(result);
	}

	/**
	 * Converts a plain boolean grant/denial into a check result.
	 *
	 * @param result {@code true} for an explicit grant, {@code false} for an explicit denial
	 * @return {@link #TRUE} or {@link #FALSE}; never {@link #UNDEFINED}
	 */
	public static PermissionCheckResult fromBoolean(final boolean result) {
		return result ? TRUE : FALSE;
	}
}
