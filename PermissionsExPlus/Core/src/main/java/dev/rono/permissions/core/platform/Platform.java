package dev.rono.permissions.core.platform;

import cloud.commandframework.CommandManager;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import dev.rono.permissions.core.util.Immutables;

/**
 * Represents a platform abstraction that provides core utilities,
 * configurations,
 * and features required for running on a specific environment.
 *
 * @param <C>
 *            the type of the sender object used for sending messages.
 */
public interface Platform<C> {
    /**
     * Provides access to the logging mechanism for this platform.
     * The returned logger can be used to record informational, warning,
     * and error messages relevant to the platform's operation.
     *
     * @return an instance of {@code PlatformLogger} for logging platform-related
     *         events and messages
     */
    PlatformLogger logger();

    /**
     * Provides access to the platform's scheduling mechanism.
     * The returned {@code PlatformScheduler} can be used to execute tasks
     * synchronously or asynchronously, schedule tasks to run after a delay,
     * or schedule repeating tasks at fixed intervals.
     *
     * @return an instance of {@code PlatformScheduler} for managing task execution
     */
    PlatformScheduler scheduler();

    /**
     * Provides access to the platform's configuration system, which includes
     * details
     * such as the data directory and other configurable resources used by the
     * platform.
     *
     * @return an instance of {@code PlatformConfiguration} for managing and
     *         retrieving
     *         platform-specific configurations
     */
    PlatformConfiguration configuration();

    /**
     * Retrieves the {@code Class} object representing the type of the sender
     * used in this platform. The sender type is utilized when sending messages
     * or performing operations that involve specific sender objects, enabling
     * type-safe handling of sender instances.
     *
     * @return the {@code Class} object representing the sender type {@code C}
     */
    Class<C> senderType();

    /**
     * Sends a message from the specified sender to the intended recipient(s).
     * The implementation of this method is platform-specific and determines
     * how the message will be delivered.
     *
     * @param sender
     *            the sender of the message; must be an instance of the platform's
     *            sender type
     * @param message
     *            the message content to be sent
     */
    void sendMessage(C sender, String message);

    /**
     * Broadcasts a message to all online operators on the platform. Operators
     * typically
     * have elevated permissions or administrative roles and are notified of
     * important
     * messages through this method.
     *
     * @param message
     *            the message content to be broadcast to the operators
     */
    default void broadcastToOperators(String message) {}

    /**
     * Creates and returns an instance of {@code CommandManager} to manage
     * commands for the platform. The command manager facilitates the registration,
     * handling, and execution of platform-specific commands.
     *
     * @return an instance of {@code CommandManager<C>} for managing commands within
     *         the platform
     * @throws Exception
     *             if an error occurs during the creation of the command manager
     */
    CommandManager<C> createCommandManager() throws Exception;

    /**
     * Retrieves a set of integrations supported or utilized by the platform.
     * Integrations represent external systems, plugins, or services that
     * are either connected to or interoperate with the platform.
     *
     * @return a set of strings representing the identifiers of the supported
     *         integrations
     */
    /**
     * Problems and states an administrator should know about, one line each,
     * such as native permission checks that PermissionsExPlus does not control.
     * Shown by {@code /pex version}.
     */
    default List<String> healthReport() {
        return Immutables.listOf();
    }

    default Set<String> integrations() {
        return Immutables.setOf();
    }

    default Set<UUID> onlineUserIds() {
        return Immutables.setOf();
    }
}
