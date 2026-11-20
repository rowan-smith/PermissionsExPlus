---
sidebar_position: 2
---

# User membership

For users, parent groups **are** their rank groups.

## Add / remove

```java
PermissionUser user = manager.getUser(player);

user.addGroup("member");
user.addGroup("donor", "survival"); // world-scoped membership
user.addGroup(manager.getGroup("vip"));

user.removeGroup("donor");
user.removeGroup("donor", "survival");
user.removeGroup(manager.getGroup("vip")); // removes in all worlds + global
```

## Replace membership

```java
// Global groups only
user.setGroups(new String[] { "member", "donor" });

// World-specific list
user.setGroups(new String[] { "builder" }, "creative");

// From PermissionGroup objects
user.setGroups(new PermissionGroup[] {
    manager.getGroup("member"),
    manager.getGroup("vip")
});
```

## Timed membership

```java
// lifetime in seconds (world may be null)
user.addGroup("vip", null, 3600L); // 1 hour of VIP
user.addGroup("event", "lobby", 900L);
```

Timed membership stores a `group-<name>-until` option and is cleaned up by the timed-group timer.

## Read membership

```java
List<PermissionGroup> groups = user.getParents();           // global
List<PermissionGroup> inWorld = user.getParents("survival");
List<String> names = user.getParentIdentifiers();
List<PermissionGroup> ownOnly = user.getOwnParents();       // directly assigned only

Map<String, List<PermissionGroup>> allWorlds = user.getAllParents();
```

:::note
`getGroups()` / `getGroupNames()` still work but are deprecated aliases of `getParents()` / `getParentIdentifiers()`.
:::

## Check membership

```java
// Defaults to checking inheritance
if (user.inGroup("staff")) { ... }

// Direct membership only
if (user.inGroup("moderator", false)) { ... }

// World + inheritance
if (user.inGroup("builder", "creative", true)) { ... }
```

## Related

- [User lookup](user-lookup)
- [Groups](groups)
- [Promote / demote](promote-demote)
