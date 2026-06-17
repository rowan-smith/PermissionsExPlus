package dev.rono.permissions.api.realm;

/**
 * Thrown when a realm name is not registered.
 *
 * <p>Raised by {@link RealmManager#getRealm(String)}.</p>
 */
public final class RealmNotFoundException extends RuntimeException {

    private final String name;

    public RealmNotFoundException(String name) {
        super("Realm not found: " + name);
        this.name = name;
    }

    /**
     * Returns the requested realm name.
     *
     * @return realm identifier that was not found
     */
    public String getName() {
        return name;
    }
}
