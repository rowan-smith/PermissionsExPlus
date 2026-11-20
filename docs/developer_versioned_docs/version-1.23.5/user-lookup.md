---
sidebar_position: 1
---

# User lookup

Work with players through `PermissionUser`. Prefer UUID / `Player` lookups when possible.

## Look up a user

```java
PermissionManager manager = PermissionsEx.getPermissionManager();

// Preferred: online player
PermissionUser user = manager.getUser(player);

// By UUID
PermissionUser byUuid = manager.getUser(player.getUniqueId());

// By name (resolves UUID when known; may return a legacy name-based record if offline/unconverted)
PermissionUser byName = manager.getUser("Steve");
```

```java
String id = user.getIdentifier(); // usually the UUID string
String name = user.getName();     // display name option / online name
boolean virtual = user.isVirtual(); // not yet persisted
```

:::tip
`getUser(String)` throws `IllegalArgumentException` if the name is null or empty. Prefer `getUser(Player)` or `getUser(UUID)` for online players.
:::

## List users

```java
// All known users (can be large on busy servers)
Set<PermissionUser> all = manager.getUsers();

// Currently loaded / online-related users
Set<PermissionUser> active = manager.getActiveUsers();

// Members of a group (null world = global; inheritance optional)
Set<PermissionUser> mods = manager.getUsers("moderator", null, true);
```

## Create / delete user records

```java
PermissionUser user = manager.getUser(player);

if (user.isVirtual()) {
    user.save(); // force a backend row (often created on first mutation anyway)
}

// Wipe user data from the backend, then drop the cached object
user.remove();
manager.resetUser(player);

// Keep the object but invalidate resolved permissions/options
manager.clearUserCache(player);
```

See [Caching](caching) for when to use each.

## Next

- [User membership](user-membership)
- [User examples](user-examples)
- [Permissions](permissions), [Options](options), [Promote / demote](promote-demote)
