/**
 * Permission subject model: shared read/write operations for users and groups.
 *
 * <p>{@link dev.rono.permissions.api.subject.PermissionSubject} composes internal roles
 * ({@link dev.rono.permissions.api.subject.SubjectIdentity},
 * {@link dev.rono.permissions.api.subject.PermissionView},
 * {@link dev.rono.permissions.api.subject.PermissionMutator}). Context-bound facades live in
 * {@link dev.rono.permissions.api.subject.SubjectContext} and typed variants
 * ({@link dev.rono.permissions.api.subject.UserContext},
 * {@link dev.rono.permissions.api.subject.GroupContext}).</p>
 */
package dev.rono.permissions.api.subject;
