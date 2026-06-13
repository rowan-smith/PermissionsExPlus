package dev.rono.permissions.core.backends.caching;

import ru.tehkode.permissions.PermissionsUserData;

import java.util.concurrent.Executor;

/**
 * User data using a cache.
 */
public class CachingUserData extends CachingData implements PermissionsUserData {
	private final PermissionsUserData userData;
	public CachingUserData(PermissionsUserData userData, Executor executor, Object lock) {
		super(executor, lock);
		this.userData = userData;
	}

	@Override
	protected PermissionsUserData getBackingData() {
		return this.userData;
	}

	@Override
	public boolean setIdentifier(final String identifier) {
		final boolean[] changed = {false};
		execute(new Runnable() {
			@Override
			public void run() {
				changed[0] = getBackingData().setIdentifier(identifier);
			}
		});
		return changed[0];
	}
}
