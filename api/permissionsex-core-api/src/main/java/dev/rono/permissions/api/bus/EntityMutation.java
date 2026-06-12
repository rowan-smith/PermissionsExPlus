package dev.rono.permissions.api.bus;

/** Domain entity mutation kinds (mirrors classic PEX entity event actions). */
public enum EntityMutation {
    PERMISSIONS_CHANGED,
    OPTIONS_CHANGED,
    INHERITANCE_CHANGED,
    INFO_CHANGED,
    TIMEDPERMISSION_EXPIRED,
    RANK_CHANGED,
    DEFAULTGROUP_CHANGED,
    WEIGHT_CHANGED,
    SAVED,
    REMOVED,
}
