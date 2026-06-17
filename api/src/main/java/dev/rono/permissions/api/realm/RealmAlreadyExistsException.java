package dev.rono.permissions.api.realm;

/**
 * Thrown when {@link RealmManager#createRealm} targets a name that is already registered.
 */
public final class RealmAlreadyExistsException extends RuntimeException {

    private final String name;

    public RealmAlreadyExistsException(String name) {
        super("Realm already exists: " + name);
        this.name = name;
    }

    /**
     * Returns the conflicting realm name.
     *
     * @return realm identifier that already exists
     */
    public String getName() {
        return name;
    }
}
