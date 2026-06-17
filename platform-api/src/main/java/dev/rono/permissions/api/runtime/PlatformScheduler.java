package dev.rono.permissions.api.runtime;

/**
 * Abstraction around platform synchronous and asynchronous task scheduling.
 *
 * <p>Delay semantics for {@link #runLater(Runnable, long)} are host-defined (Minecraft ticks on Spigot).</p>
 */
public interface PlatformScheduler {

    /**
     * Runs {@code task} on the platform main/synchronous thread.
     *
     * @param task work to execute
     */
    void runSync(Runnable task);

    /**
     * Runs {@code task} asynchronously when the platform supports it.
     *
     * @param task work to execute
     */
    void runAsync(Runnable task);

    /**
     * Schedules {@code task} to run after a host-defined delay.
     *
     * @param task       work to execute
     * @param delayTicks delay in platform tick units
     */
    void runLater(Runnable task, long delayTicks);

    /**
     * Schedules {@code task} after {@code delaySeconds}. Default converts seconds to 20 ticks per second.
     */
    default void runLaterSeconds(Runnable task, long delaySeconds) {
        runLater(task, delaySeconds * 20L);
    }
}
