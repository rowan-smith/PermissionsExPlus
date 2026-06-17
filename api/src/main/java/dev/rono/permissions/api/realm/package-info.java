/**
 * Realm registry and namespace helpers.
 *
 * <p>A <em>realm</em> in PermissionsEx is a permission namespace (global or named), not necessarily
 * a loaded Bukkit dimension. On proxies, realm ids are often backend server names. Use
 * {@link dev.rono.permissions.api.permission.PermissionContext} for scoped checks; use
 * {@link RealmManager} for inheritance administration.</p>
 */
package dev.rono.permissions.api.realm;
