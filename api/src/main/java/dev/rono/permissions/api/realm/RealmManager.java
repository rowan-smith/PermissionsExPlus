package dev.rono.permissions.api.realm;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Realm registry with explicit find/get/create/exists lifecycle and inheritance administration.
 *
 * <p>Realms are permission namespaces stored in the backend. {@link #count()} may include entries
 * that do not correspond to a loaded server dimension.</p>
 */
public interface RealmManager {

    /**
     * Looks up a registered realm without creating a record.
     *
     * @param name realm identifier
     * @return the realm when registered; empty if absent
     */
    Optional<Realm> findRealm(String name);

    /**
     * Returns a registered realm by name.
     *
     * @param name realm identifier
     * @return live realm adapter
     * @throws RealmNotFoundException if {@code name} is not registered
     */
    Realm getRealm(String name) throws RealmNotFoundException;

    /**
     * Registers a new realm namespace with an empty inheritance chain.
     *
     * @param name realm identifier
     * @return live realm adapter for the new record
     * @throws RealmAlreadyExistsException if {@code name} is already registered
     */
    Realm createRealm(String name) throws RealmAlreadyExistsException;

    /**
     * Reports whether a realm is registered.
     *
     * @param name realm identifier
     * @return {@code true} if the realm exists in the registry
     */
    boolean exists(String name);

    /**
     * Returns all registered realm names sorted lexicographically.
     *
     * @return immutable sorted realm name list
     */
    List<String> listRealmNames();

    /**
     * Returns live adapters for all registered realms.
     *
     * @return realm list in the same order as {@link #listRealmNames()}
     */
    default List<Realm> listRealms() {
        return listRealmNames().stream().map(this::getRealm).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns the number of registered realms.
     *
     * @return total registered realm count
     */
    int count();

    /**
     * Returns how many registered realms match {@code filter}.
     *
     * @param filter predicate applied to each registered realm; must not be {@code null}
     * @return count of realms for which the predicate is {@code true}
     */
    int count(Predicate<Realm> filter);
}
