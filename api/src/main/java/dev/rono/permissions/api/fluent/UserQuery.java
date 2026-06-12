package dev.rono.permissions.api.fluent;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.UserWorldContext;
import java.util.UUID;

/**
 * Fluent user lookup — obtain via {@link PermissionService#user()} or {@link WorldQuery#user()}.
 *
 * <pre>{@code
 * pex.user().by(uuid).inWorld(world).inGroup("vip", true);
 * pex.world(world).user().by(uuid).inGroup("vip", true);
 * }</pre>
 */
public final class UserQuery {

    private final PermissionService service;
    private final String presetWorld;

    UserQuery(PermissionService service, String presetWorld) {
        this.service = service;
        this.presetWorld = presetWorld;
    }

    public static UserQuery resolve(PermissionService service) {
        return new UserQuery(service, null);
    }

    public static UserQuery inWorld(PermissionService service, String world) {
        return new UserQuery(service, world);
    }

    /** Resolve or materialize by UUID. */
    public ResolvedUser by(UUID uuid) {
        return new ResolvedUser(service, uuid, null);
    }

    /** Resolve or materialize by identifier (name or UUID string). */
    public ResolvedUser by(String identifier) {
        return new ResolvedUser(service, null, identifier);
    }

    /** Alias for {@link #by(UUID)}. */
    public ResolvedUser uuid(UUID uuid) {
        return by(uuid);
    }

    /** Alias for {@link #by(String)}. */
    public ResolvedUser named(String identifier) {
        return by(identifier);
    }

    /**
     * When this query is scoped to a world ({@link WorldQuery#user()}), returns the world context
     * directly — no {@link ResolvedUser#inWorld(String)} needed.
     */
    public UserWorldContext byWorld(UUID uuid) {
        return presetWorld != null
                ? new ResolvedUser(service, uuid, null).inPresetWorld(presetWorld)
                : new ResolvedUser(service, uuid, null).global();
    }

    /** {@link #byWorld(UUID)} for a name/UUID-string identifier. */
    public UserWorldContext byWorld(String identifier) {
        return presetWorld != null
                ? new ResolvedUser(service, null, identifier).inPresetWorld(presetWorld)
                : new ResolvedUser(service, null, identifier).global();
    }
}
