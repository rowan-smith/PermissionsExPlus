package dev.rono.permissions.api.runtime;

import java.util.UUID;

/** Maps online actors to realms/world scopes (thin companion to {@linkplain PlatformAdapter#onlineRealm}). */
@FunctionalInterface
public interface ContextResolver {

    /**
     * @return active realm slug for {@code holder}, or {@code null} whenever offline/disconnected.
     */
    String realmFor(UUID holder);
}
