package dev.rono.permissions.core.backends;

import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import ru.tehkode.permissions.backends.PermissionBackend;
/**
 * Backend for permission
 *
 * Default group:
 * Groups have a default flag. All users are in groups with default marked true.
 * No default group is required to exist.
 */
public abstract class AbstractPermissionBackend implements PermissionBackend {
	private final PermissionManager manager;
	private final PEXBackendConfiguration backendConfig;
	/**
	 * Executor currently being used to execute backend tasks
	 */
	private volatile Executor activeExecutor;
	/**
	 * Executor that consistently maintains a reference to the executor actively being used
	 */
	private final Executor activeExecutorPtr,
			onThreadExecutor;
	private final ExecutorService asyncExecutor;
	private final List<SchemaUpdate> schemaUpdates = new LinkedList<>();

	protected AbstractPermissionBackend(PermissionManager manager, PEXBackendConfiguration backendConfig) throws PermissionBackendException {
		this.manager = manager;
		this.backendConfig = backendConfig;
		this.asyncExecutor = Executors.newSingleThreadExecutor();
		this.onThreadExecutor = new Executor() {
			@Override
			public void execute(Runnable runnable) {
				runnable.run();
			}
		};
		this.activeExecutor = asyncExecutor; // Default

		this.activeExecutorPtr = new Executor() {
			@Override
			public void execute(Runnable runnable) {
				AbstractPermissionBackend.this.activeExecutor.execute(runnable);
			}
		};
	}

	protected void addSchemaUpdate(SchemaUpdate update) {
		schemaUpdates.add(update);
		Collections.sort(schemaUpdates);
	}

	protected void performSchemaUpdate() {
		int version = getSchemaVersion();
		int newVersion = version;
		try {
			for (SchemaUpdate update : schemaUpdates) {
				try {
					if (update.getUpdateVersion() <= version) {
						continue;
					}
					if (newVersion == version) { // No updates have been performed yet
						backupDatabase();
					}
					update.performUpdate();
					newVersion = Math.max(update.getUpdateVersion(), newVersion);
				} catch (Throwable t) {
					getLogger().warning("While updating to " + update.getUpdateVersion() + " from " + newVersion + ": " + t.getMessage());
					break;
				}
			}
		} finally {
			if (newVersion != version) {
				setSchemaVersion(newVersion);
			}
		}
	}

	protected void backupDatabase() throws IOException {
		try (Writer w = new FileWriter(new File(manager.getBasedir(), getConfig().getName() + "-backup." + getSchemaVersion() + ".bak"))) {
			writeContents(w);
		}
	}

	/**
	 * Return the current schema version.
	 * -1 indicates that the schema version is unknown.
	 *
	 * @return The current schema version
	 */
	public abstract int getSchemaVersion();

	/**
	 * Update the schema version. May be a no-op for unversioned schemas.
	 *
	 * @param version The new version
	 */
	protected abstract void setSchemaVersion(int version);

	public int getLatestSchemaVersion() {
		if (schemaUpdates.isEmpty()) {
			return -1;
		}
		return schemaUpdates.get(schemaUpdates.size() - 1).getUpdateVersion();
	}

	protected Executor getExecutor() {
		return activeExecutorPtr;
	}

	protected final PermissionManager getManager() {
		return manager;
	}

	protected final PEXBackendConfiguration getConfig() {
		return backendConfig;
	}

	public abstract void reload() throws PermissionBackendException;

	public abstract PermissionsUserData getUserData(String userName);

	public abstract PermissionsGroupData getGroupData(String groupName);

	public abstract boolean hasUser(String userName);

	public abstract boolean hasGroup(String group);

	/**
	 * Return list of identifiers associated with users. These may not be user-readable
	 * @return Identifiers associated with users
	 */
	public abstract Collection<String> getUserIdentifiers();

	/**
	 * Return friendly names of known users. These cannot be passed to {@link #getUserData(String)} to return a valid user object
	 * @return Names associated with users
	 */
	public abstract Collection<String> getUserNames();

	/*public List<PermissionsUserData> getUsers() {
		List<PermissionsUserData> userData = new ArrayList<PermissionsUserData>();
		for (String name : getUserNames()) {
			userData.add(getUserData(name));
		}
		return Collections.unmodifiableList(userData);
	}*/

	public abstract Collection<String> getGroupNames();

	/*public List<PermissionsGroupData> getGroups() {
		List<PermissionsGroupData> groupData = new ArrayList<PermissionsGroupData>();
		for (String name : getGroupNames()) {
			groupData.add(getGroupData(name));
		}
		Collections.sort(groupData);
		return Collections.unmodifiableList(groupData);
	}*/

	// -- World inheritance

	public abstract List<String> getWorldInheritance(String world);

	public abstract Map<String, List<String>> getAllWorldInheritance();

	public abstract void setWorldInheritance(String world, List<String> inheritance);

	public void close() throws PermissionBackendException {
		asyncExecutor.shutdown();
		try {
			if (!asyncExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
				getLogger().warning("All backend tasks not completed after 30 seconds, waiting 2 minutes.");
				if (!asyncExecutor.awaitTermination(2, TimeUnit.MINUTES)) {
					getLogger().warning("All backend tasks not completed after another 2 minutes, giving up on the wait.");
				}
			}
		} catch (InterruptedException e) {
			throw new PermissionBackendException(e);
		}
	}

	public final Logger getLogger() {
		return manager.getLogger();
	}

	/**
	 * Load data from alternate backend.
	 * Assume that this overwrites all data in the receiving backend (except for users not included in transferring backend)
	 *
	 * @param backend The backend to load data from
	 */
	public void loadFrom(PermissionBackend backend) {
		setPersistent(false);
		try {
			for (String group : backend.getGroupNames()) {
				BackendDataTransfer.transferGroup(backend.getGroupData(group), getGroupData(group));
			}

			for (String user : backend.getUserIdentifiers()) {
				BackendDataTransfer.transferUser(backend.getUserData(user), getUserData(user));
			}

			for (Map.Entry<String, List<String>> ent : backend.getAllWorldInheritance().entrySet()) {
				setWorldInheritance(ent.getKey(), ent.getValue()); // Could merge data but too complicated & too lazy
			}
		} finally {
			setPersistent(true);
		}
	}


	public void revertUUID() {
		this.setPersistent(false);
		try {
			for (String ident : getUserIdentifiers()) {
				PermissionsUserData data = getUserData(ident);
				String name = data.getOption("name", null);
				if (name != null) {
					data.setIdentifier(name);
					data.setOption("name", null, null);
				}
			}
		} finally {
			this.setPersistent(true);
		}
	}

	public void setPersistent(boolean persistent) {
		if (persistent) {
			this.activeExecutor = asyncExecutor;
		} else {
			this.activeExecutor = onThreadExecutor;
		}
	}

	/**
	 * Allow this backend to write its contents to a file.
	 * @param writer The writer to dump contents to.
	 */
	public abstract void writeContents(Writer writer) throws IOException;

	/** Startup banner snippet for the active storage stack (defaults to the simple class name). */
	public String diagnosticLabel() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{config=" + getConfig().getName() + "}";
	}

}
