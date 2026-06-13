/**
 * Modern permission event bus for hook plugins.
 *
 * <p>Dispatches are synchronous on the publisher thread. On Spigot/Paper, entity and system
 * dispatches are also translated into legacy Bukkit events when the platform adapter publishes
 * them.</p>
 *
 * @see dev.rono.permissions.api.event.PermissionEventBus
 */
package dev.rono.permissions.api.event;
