package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.permission.PermissionContext;
import java.util.List;

/**
 * Shared permission-subject operations for users and groups.
 *
 * <p>Scoped operations take {@link PermissionContext}. Use {@link #inContext(PermissionContext)} for
 * a fixed-scope facade, or {@link #global()} for the global namespace.</p>
 */
public interface PermissionSubject extends SubjectIdentity, PermissionView, PermissionMutator {

    /** @return context-bound facade for this subject */
    SubjectContext inContext(PermissionContext context);

    /** @return facade bound to {@link PermissionContext#global()} */
    default SubjectContext global() {
        return inContext(PermissionContext.global());
    }

    /** @return all timed permission entries across every configured realm, including global */
    List<TimedPermissionEntry> allTimedPermissionEntries();
}
