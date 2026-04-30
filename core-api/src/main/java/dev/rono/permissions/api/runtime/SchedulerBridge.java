package dev.rono.permissions.api.runtime;

/** Abstraction around Bukkit synchronous/main-thread scheduling. */
public interface SchedulerBridge {

    void runSync(Runnable task);

    void runAsync(Runnable task);

    /** Delay in Minecraft ticks semantics are host-defined. */
    void runLater(Runnable task, long delayTicks);
}
