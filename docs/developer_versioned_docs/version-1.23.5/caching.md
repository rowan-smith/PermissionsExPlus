---
sidebar_position: 9
---

# Caching

`PermissionManager` keeps `PermissionUser` / `PermissionGroup` objects in memory. Knowing when to **reset** vs **clear cache** avoids stale data after external edits or bulk changes.

## `clearUserCache` vs `resetUser`

| Method | Effect |
|--------|--------|
| `clearUserCache(Player\|UUID\|String)` | Keep the `PermissionUser` instance; clear permission / option / group answer caches |
| `resetUser(Player\|String)` | Remove the user from the manager map so the next `getUser` reloads from the backend |

```java
PermissionManager manager = PermissionsEx.getPermissionManager();

// After you changed data through the same PermissionUser instance, caches
// are usually updated already. Use clearUserCache when something else
// invalidated answers but the object should stay:
manager.clearUserCache(player);

// After deleting a user, or if another process rewrote their backend row:
user.remove();
manager.resetUser(player);

// Same idea for groups
group.remove();
manager.resetGroup(group.getIdentifier());
```

Do **not** treat `clearUserCache` and `resetUser` as interchangeable.

## Warm cache on login

PEX itself calls `cacheUser` during async login. Plugins that resolve users very early can do the same:

```java
// identifier = UUID string, fallbackName = last known name
manager.cacheUser(player.getUniqueId().toString(), player.getName());
```

## Listing users

```java
Set<PermissionUser> loaded = manager.getActiveUsers(); // currently in the manager map
Set<PermissionUser> all = manager.getUsers();          // every user known to the backend
```

`getUsers()` can be expensive on large SQL backends. Prefer `getActiveUsers()` or `getUsers(group, world, inheritance)` when you only need a subset.

## When to clear

Clear or reset after:

- External edits to `permissions.yml` / SQL outside the API (then prefer `manager.reset()`)
- Your plugin bulk-updated many users and holds onto old `PermissionUser` references
- You called low-level backend APIs that bypass entity setters

Normal `addPermission` / `addGroup` / `setOption` paths already clear the affected entity's cache.
