package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import dev.rono.permissions.api.subject.PexGroup;
import dev.rono.permissions.api.subject.PexGroupWorldContext;
import dev.rono.permissions.api.subject.PexUserWorldContext;
import dev.rono.permissions.api.world.PexWorlds;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * World-scoped chain — obtain via {@link dev.rono.permissions.api.service.PermissionService#world(String)}.
 *
 * <h2>Resolve vs find</h2>
 * <ul>
 *   <li><strong>Resolve</strong> ({@link #user(UUID)}, {@link #user(String)}, {@link #group(String)}):
 *       returns a world-bound subject context, materializing the underlying record when it does not
 *       yet exist in the backend.</li>
 *   <li><strong>Find</strong> ({@link #findUser(UUID)}, {@link #findUser(String)},
 *       {@link #findGroup(String)}): returns empty when the subject is not persisted; no record is
 *       created.</li>
 * </ul>
 *
 * <pre>{@code
 * pex.world(world).user(uuid).inGroup("vip", true);
 * pex.world(world).findUser(uuid).map(u -> u.hasPermission("node")).orElse(false);
 * }</pre>
 */
public final class PexWorldScope {

    private final PermissionServiceBridge service;
    private final String world;

    public PexWorldScope(PermissionServiceBridge service, String world) {
        this.service = service;
        this.world = PexWorlds.normalize(world);
    }

    /**
     * Returns the normalized world name for this scope.
     *
     * @return {@link PexWorlds#GLOBAL} ({@code null}) or a specific trimmed world name
     */
    public String name() {
        return world;
    }

    /**
     * Reports whether this scope targets the global (all-worlds) namespace.
     *
     * @return {@code true} when {@link #name()} is global
     */
    public boolean isGlobal() {
        return PexWorlds.isGlobal(world);
    }

    /**
     * Returns the inheritance chain for this world (parent worlds whose permissions apply).
     *
     * @return ordered list of parent world names
     */
    public List<String> inheritance() {
        return service.worldInheritance(world);
    }

    /**
     * Sets the inheritance chain for this world.
     *
     * @param parentWorlds ordered list of parent world names
     */
    public void setInheritance(List<String> parentWorlds) {
        service.setWorldInheritance(world, parentWorlds);
    }

    /**
     * Returns groups marked as default for new users in this world.
     *
     * @return default groups for this scope's world
     */
    public List<PexGroup> defaultGroups() {
        return service.defaultGroups(world);
    }

    /**
     * Returns the rank ladder mapping for a named ladder in this installation.
     *
     * @param ladderName ladder identifier
     * @return map from rank index to group at that rank
     */
    public Map<Integer, PexGroup> rankLadder(String ladderName) {
        return service.rankLadder(ladderName);
    }

    /**
     * Resolves a user in this world, materializing a backend record when none exists yet.
     *
     * @param uuid player UUID
     * @return a {@link PexUserWorldContext} preset to {@link #name()}
     */
    public PexUserWorldContext user(UUID uuid) {
        return SubjectRefs.user(service, uuid, null).inPresetWorld(world);
    }

    /**
     * Resolves a user in this world, materializing a backend record when none exists yet.
     *
     * @param identifier user name or UUID string
     * @return a {@link PexUserWorldContext} preset to {@link #name()}
     */
    public PexUserWorldContext user(String identifier) {
        return SubjectRefs.user(service, null, identifier).inPresetWorld(world);
    }

    /**
     * Looks up a persisted user in this world without materializing a new record.
     *
     * @param uuid player UUID
     * @return a world-bound context when the user exists in the backend, otherwise empty
     */
    public Optional<PexUserWorldContext> findUser(UUID uuid) {
        return SubjectRefs.user(service, uuid, null).findInPresetWorld(world);
    }

    /**
     * Looks up a persisted user in this world without materializing a new record.
     *
     * @param identifier user name or UUID string
     * @return a world-bound context when the user exists in the backend, otherwise empty
     */
    public Optional<PexUserWorldContext> findUser(String identifier) {
        return SubjectRefs.user(service, null, identifier).findInPresetWorld(world);
    }

    /**
     * Resolves a group in this world, materializing a backend record when none exists yet.
     *
     * @param name group name
     * @return a {@link PexGroupWorldContext} preset to {@link #name()}
     */
    public PexGroupWorldContext group(String name) {
        return SubjectRefs.group(service, name).inPresetWorld(world);
    }

    /**
     * Looks up a persisted group in this world without materializing a new record.
     *
     * @param name group name
     * @return a world-bound context when the group exists in the backend, otherwise empty
     */
    public Optional<PexGroupWorldContext> findGroup(String name) {
        return SubjectRefs.group(service, name).findInPresetWorld(world);
    }
}
