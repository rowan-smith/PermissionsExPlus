---
sidebar_position: 1
---

# Developer Overview

PermissionsExPlus exposes a Bukkit API for plugins that need to manage users, groups, permissions, options, and rank ladders at runtime.

Use this section if you are writing a plugin that integrates with PermissionsExPlus. For server setup and commands, see [Documentation](/docs/intro).

## Quick start

1. [Install the API](installing) as a `provided` / `compileOnly` dependency
2. [Declare a soft dependency](depending) and resolve `PermissionManager` at runtime
3. Browse the guides below

| Area | Start here |
|------|------------|
| Users | [Lookup](user-lookup) · [Membership](user-membership) · [Examples](user-examples) |
| Groups | [Groups](groups) · [Inheritance](group-inheritance) · [Ladders](ladders) · [Promote](promote-demote) |
| Permissions | [Nodes](permissions) · [Checks](permission-checks) · [Options](options) · [Prefix & weight](prefix-suffix-weight) |
| Advanced | [Events](events) · [Worlds](worlds) · [Backends](backends) · [Debug](debug-and-matching) · [Caching](caching) |

## Core types

| Class | Role |
|-------|------|
| `PermissionManager` | Entry point: users, groups, ladders, permission checks |
| `PermissionUser` | A player (by name or UUID) |
| `PermissionGroup` | A group: weight, rank, ladder, inheritance |
| `PermissionEntity` | Shared base for users and groups (permissions, options, prefix/suffix) |

## Minimal example

```java
@Override
public void onEnable() {
    if (!PermissionsEx.isAvailable()) {
        return;
    }

    PermissionManager manager = PermissionsEx.getPermissionManager();

    getServer().getPluginManager().registerEvents(new Listener() {
        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            PermissionUser user = manager.getUser(event.getPlayer());
            if (!user.inGroup("member") && !user.inGroup("newcomer")) {
                user.addGroup("newcomer");
            }
        }
    }, this);
}
```

## Persistence

Most mutating calls (`addPermission`, `setOption`, `addGroup`, `setRank`, and so on) write through to the active backend immediately.

`getGroup(name)` / `getUser(...)` may return a **virtual** entity that is not stored yet. Call `save()` to create it, or any mutating call that triggers a backend save will persist it. Check with `isVirtual()`.

```java
PermissionGroup group = manager.getGroup("vip");
if (group.isVirtual()) {
    group.save();
}

PermissionUser user = manager.getUser(player);
if (user.isVirtual()) {
    user.addGroup("default"); // first write typically persists the user
}
```

```java
group.remove();
manager.resetGroup(group.getIdentifier());

user.remove();
manager.resetUser(player);
```

`PermissionGroup.remove()` also clears that group from users' membership lists and from other groups' parent lists. See [Caching](caching) for `resetUser` vs `clearUserCache`.
