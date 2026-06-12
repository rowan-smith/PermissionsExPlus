package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.world.Worlds;
import java.util.List;
import java.util.Map;

/**
 * World-scoped view of a {@link PermissionSubject}.
 *
 * <p>Obtained via {@link PermissionSubject#inWorld(String)}. {@link Worlds#GLOBAL} applies to the
 * global namespace.</p>
 */
public interface SubjectWorldContext {

    /** {@link Worlds#GLOBAL} or a specific world name. */
    String world();

    PermissionSubject subject();

    boolean has(String permission);

    List<String> permissions();

    List<String> effectivePermissions();

    void addPermission(String permission);

    void removePermission(String permission);

    void setPermissions(List<String> permissions);

    void addTimedPermission(String permission, int lifetimeSeconds);

    void removeTimedPermission(String permission);

    List<String> timedPermissions();

    List<TimedPermissionEntry> timedPermissionEntries();

    int timedPermissionRemainingSeconds(String permission);

    boolean hasTimedPermission(String permission);

    String prefix();

    String suffix();

    void setPrefix(String prefix);

    void setSuffix(String suffix);

    String option(String key);

    void setOption(String key, String value);

    Map<String, String> options();
}
