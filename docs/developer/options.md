---
sidebar_position: 3
---

# Options

Options are key-value metadata on users and groups.

## Set and get

```java
PermissionGroup group = manager.getGroup("vip");

group.setOption("color", "gold");
group.setOption("fly", "true", "creative"); // world-scoped

String color = group.getOption("color"); // may resolve via inheritance
String own = group.getOwnOption("color"); // direct value only
String withDefault = group.getOption("color", null, "white");

int seats = group.getOptionInteger("max-homes", null, 1);
boolean fly = group.getOptionBoolean("fly", "creative", false);
double chance = group.getOptionDouble("drop-multiplier", null, 1.0);
```

### Remove an option

Pass `null` as the value:

```java
group.setOption("color", null);
group.setOption("fly", null, "creative");
```

### Bulk read

```java
Map<String, String> options = group.getOptions(null);
Map<String, Map<String, String>> all = group.getAllOptions();
```

Common keys: `prefix`, `suffix`, `weight`, `rank`, `rank-ladder`, `default`, plus custom keys for chat plugins. See [Options](/docs/next/concepts-guides/options).

## User options override groups

```java
PermissionUser user = manager.getUser(player);

user.setOption("prefix", "&c[Donor] ");
user.setOption("max-homes", "5");

String world = player.getWorld().getName();
String prefix = user.getOption("prefix", world, "");
int homes = user.getOptionInteger("max-homes", world, 1);
```

### Feature flag stored as an option

```java
public void unlockPet(Player player, String petId) {
    PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
    user.setOption("pet." + petId, "true");
}

public boolean hasPet(Player player, String petId) {
    PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
    return user.getOptionBoolean("pet." + petId, null, false);
}
```

Next: [Prefix, suffix & weight](prefix-suffix-weight).
