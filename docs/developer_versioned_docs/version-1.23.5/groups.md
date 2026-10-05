---
sidebar_position: 1
---

# Groups

Manage permission groups through `PermissionManager` and `PermissionGroup`. For assigning players to groups, see [User membership](user-membership).

## Create a group

`getGroup(name)` returns a group object. New names come back as **virtual** until saved (or until a mutating call persists them).

```java
PermissionManager manager = PermissionsEx.getPermissionManager();
PermissionGroup group = manager.getGroup("moderator");

if (!group.isVirtual()) {
    // already exists in the backend
    return;
}

group.setWeight(50);
group.setPrefix("&9[Mod] ", null);
group.addPermission("essentials.kick");
group.addPermission("essentials.mute");
group.save();
```

:::note
`getGroup` returns `null` only for a null/empty name. For normal names the file/SQL backends return a virtual group object instead of `null`.
:::

### Create with parents

```java
PermissionGroup helper = manager.getGroup("helper");
PermissionGroup moderator = manager.getGroup("moderator");

if (moderator.isVirtual()) {
    moderator.setParents(List.of(helper));
    moderator.save();
}

// Or by name:
moderator.setParentsIdentifier(List.of("helper"));
```

## Delete a group

```java
PermissionGroup group = manager.getGroup("moderator");
if (group.isVirtual()) {
    return; // never persisted
}

group.remove(); // also removes this group from users and child-group parents
manager.resetGroup(group.getIdentifier()); // drop manager cache
```

## List groups

```java
List<PermissionGroup> groups = manager.getGroupList();
for (PermissionGroup group : groups) {
    getLogger().info(group.getIdentifier()
            + " weight=" + group.getWeight()
            + " rank=" + group.getRank()
            + " ladder=" + group.getRankLadder());
}
```

## Default groups

```java
List<PermissionGroup> defaults = manager.getDefaultGroups(null); // global
List<PermissionGroup> worldDefaults = manager.getDefaultGroups("survival");

PermissionGroup group = manager.getGroup("default");
group.setDefault(true, null);              // default in all worlds
group.setDefault(true, "minigames");       // world-scoped default
boolean isDefault = group.isDefault(null);
```

## Users in a group

```java
PermissionGroup group = manager.getGroup("moderator");

Set<PermissionUser> members = group.getUsers();       // stored members
Set<PermissionUser> online = group.getActiveUsers();  // loaded/active

// Via manager
Set<PermissionUser> withInherit = manager.getUsers("staff", null, true);
Set<PermissionUser> directOnly = manager.getUsers("staff", null, false);
```

Assigning players is done on the user object:

```java
manager.getUser(player).addGroup("moderator");
manager.getUser(player).removeGroup("moderator");
```

See [User membership](user-membership) and [Group inheritance](group-inheritance).
