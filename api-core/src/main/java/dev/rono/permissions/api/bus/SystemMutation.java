package dev.rono.permissions.api.bus;

/** System-level permission engine notifications. */
public enum SystemMutation {
    /** Active backend configuration or instance changed. */
    BACKEND_CHANGED,
    /** In-memory state cleared and backend data reloaded. */
    RELOADED,
    /** World inheritance graph changed. */
    WORLDINHERITANCE_CHANGED,
    /** Default group assignment changed. */
    DEFAULTGROUP_CHANGED,
    /** Debug logging mode toggled. */
    DEBUGMODE_TOGGLE,
    /** Spigot superperms bridge reinjected permissibles. */
    REINJECT_PERMISSIBLES,
}
