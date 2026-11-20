---
sidebar_position: 2
---

# Permission checks

How to test whether a user/group has a node, plus short recipes.

## Check permissions

```java
boolean ok = user.has("essentials.fly");
boolean inWorld = user.has("essentials.fly", "creative");

boolean viaManager = manager.has(player, "essentials.fly");
boolean viaManagerWorld = manager.has(player, "essentials.fly", "creative");
boolean byUuid = manager.has(player.getUniqueId(), "essentials.fly", "survival");
boolean globalOnly = manager.has(player.getUniqueId(), "essentials.fly", null); // no world pass
boolean byName = manager.has("Steve", "essentials.fly", "survival");
```

:::note
`user.has(permission)` with no world uses the player's current world when online. If the user is offline, it falls back to the server's first loaded world. Prefer an explicit world name when that matters.

`manager.has(..., null)` means **global-only** resolution (no world or world-inheritance pass), not "current world".
:::

Bukkit `player.hasPermission(...)` is also bridged: PermissionsExPlus injects its Superperms handler on enable, so both the PEX API and Bukkit checks go through PEX. The APIs are not identical in edge cases (attachments, defaults), so use the PEX `has` methods when you need PEX's exact resolution.

## Examples

### Event reward

```java
public void grantEventReward(Player player) {
    PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
    user.addTimedPermission("essentials.fly", null, 1800);
    user.addTimedPermission("essentials.kit.event", null, 1800);
    user.addPermission("event.participant"); // permanent flag
}
```

### World-only build rights

```java
PermissionUser user = manager.getUser(player);
user.addPermission("worldedit.*", "creative");
user.addPermission("-worldedit.schematic", "creative");
```

### Clone a group's permission list onto a user

```java
PermissionGroup template = manager.getGroup("builder");
PermissionUser user = manager.getUser(player);

List<String> nodes = new ArrayList<>(template.getOwnPermissions(null));
user.setPermissions(nodes);
```

See also [Permissions](permissions) and [Debug & matching](debug-and-matching).
