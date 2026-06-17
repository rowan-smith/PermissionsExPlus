package dev.rono.permissions.api.bus;

/**
 * Domain entity mutation kinds (mirrors classic PEX entity event actions).
 */
public enum EntityMutation {
    /** Direct or inherited permission assignments changed. */
    PERMISSIONS_CHANGED,
    /** Subject options/metadata changed. */
    OPTIONS_CHANGED,
    /** Parent group or world inheritance links changed. */
    INHERITANCE_CHANGED,
    /** Display info (name, etc.) changed. */
    INFO_CHANGED,
    /** A timed permission grant expired and was removed. */
    TIMEDPERMISSION_EXPIRED,
    /** Rank ladder assignment changed. */
    RANK_CHANGED,
    /** Default-group flag changed. */
    DEFAULTGROUP_CHANGED,
    /** Group weight changed. */
    WEIGHT_CHANGED,
    /** Subject persisted to the backend. */
    SAVED,
    /** Subject removed from the backend. */
    REMOVED,
}
