package dev.rono.permissions.api.bus;

/** System-level permission engine notifications. */
public enum SystemMutation {
    BACKEND_CHANGED,
    RELOADED,
    WORLDINHERITANCE_CHANGED,
    DEFAULTGROUP_CHANGED,
    DEBUGMODE_TOGGLE,
    REINJECT_PERMISSIBLES,
}
