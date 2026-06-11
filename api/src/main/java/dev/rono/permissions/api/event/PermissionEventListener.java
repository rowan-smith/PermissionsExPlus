package dev.rono.permissions.api.event;

import dev.rono.permissions.api.bus.EntityDispatch;
import dev.rono.permissions.api.bus.SystemDispatch;

/** Listener for modern permission bus dispatches (parallel to legacy Bukkit events on Spigot). */
public interface PermissionEventListener {

    default void onEntity(EntityDispatch dispatch) {}

    default void onSystem(SystemDispatch dispatch) {}
}
