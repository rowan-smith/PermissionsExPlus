---
sidebar_position: 3
---

# User examples

Ready-to-adapt recipes. Assumes you already resolved a `PermissionManager` (see [Depending](depending)).

## Welcome kit on first join

```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    if (!PermissionsEx.isAvailable()) {
        return;
    }

    PermissionManager manager = PermissionsEx.getPermissionManager();
    PermissionUser user = manager.getUser(event.getPlayer());

    if (user.getOwnParents().isEmpty() && user.isVirtual()) {
        user.addGroup("newcomer");
        user.setPrefix("&7[New] ", null);
        user.addTimedPermission("essentials.kit.starter", null, 86400);
    }
}
```

## Temporary VIP purchase

```java
public void grantVip(Player player, long seconds) {
    PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
    user.addGroup("vip", null, seconds);
    user.setOption("vip-source", "webstore");
    player.sendMessage("VIP enabled for " + seconds + " seconds");
}

public void revokeVip(Player player) {
    PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
    user.removeGroup("vip");
    user.setOption("vip-source", null);
}
```

## Staff check helper

```java
public boolean isStaff(Player player) {
    PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
    return user.inGroup("helper")
            || user.inGroup("moderator")
            || user.inGroup("admin");
}

public boolean canModerate(Player staff, Player target) {
    PermissionUser a = PermissionsEx.getPermissionManager().getUser(staff);
    PermissionUser b = PermissionsEx.getPermissionManager().getUser(target);
    // lower rank number = higher on ladder
    return a.isRanked("staff") && b.isRanked("staff")
            && a.getRank("staff") < b.getRank("staff");
}
```

## Dump a user's state

```java
public void dumpUser(Player player) {
    PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
    String world = player.getWorld().getName();

    getLogger().info("User " + user.getName() + " (" + user.getIdentifier() + ")");
    getLogger().info("  prefix=" + user.getPrefix(world));
    getLogger().info("  groups=" + user.getParentIdentifiers(world));
    getLogger().info("  own perms=" + user.getOwnPermissions(world));
    getLogger().info("  has fly=" + user.has("essentials.fly", world));
}
```
