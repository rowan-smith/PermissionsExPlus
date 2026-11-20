---
sidebar_position: 7
---

# World Inheritance

World inheritance lets one world fall back to another world's permissions and options (then to global). This is separate from per-world nodes on a user or group.

For the admin concept guide, see [World Permissions](/docs/concepts-guides/world-permissions).

## Read inheritance

```java
PermissionManager manager = PermissionsEx.getPermissionManager();

List<String> parents = manager.getWorldInheritance("world_nether");
// e.g. ["world"] means nether falls back to overworld, then global
```

Empty list means no world parents (global only after the world's own data).

## Set inheritance

```java
manager.setWorldInheritance("world_nether", List.of("world"));
manager.setWorldInheritance("world_the_end", List.of("world"));

// Clear
manager.setWorldInheritance("world_nether", List.of());
```

This clears active user caches and fires `PermissionSystemEvent.Action.WORLDINHERITANCE_CHANGED`.

## How checks resolve

When you call `has(permission, world)` (or read options/prefix for a world), PermissionsExPlus roughly walks:

1. Data for that world on the user/group
2. Inherited parent worlds (in order)
3. Global (no-world) data
4. Parent groups (with the same world context)

So a permission granted only in `world` can apply in `world_nether` if nether inherits from `world`.

## Example: hub shares survival perms

```java
public void setupWorldTree(PermissionManager manager) {
    // creative has its own nodes; survival is the shared base
    manager.setWorldInheritance("world_nether", List.of("world"));
    manager.setWorldInheritance("world_the_end", List.of("world"));
    manager.setWorldInheritance("plots", List.of()); // standalone
}
```

## Per-world membership still wins

World inheritance does **not** replace world-scoped group membership:

```java
user.addGroup("builder", "plots");     // only in plots
user.addPermission("vip.fly", "lobby"); // only in lobby
```

Use inheritance for shared world trees; use world-scoped nodes/groups for exceptions.
