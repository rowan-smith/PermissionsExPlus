---
sidebar_position: 2
---

# Depending

Declare PermissionsExPlus as a soft dependency, then resolve the API only when the plugin is present and enabled.

## plugin.yml

The Bukkit plugin name is `PermissionsEx` (not PermissionsExPlus):

```yaml
name: MyPlugin
main: com.example.MyPlugin
version: 1.0.0
api-version: 1.20
softdepend: [PermissionsEx]
```

Use `softdepend` so your plugin still loads if PermissionsExPlus is missing. Use `depend` only if your plugin cannot run without it.

## Check availability

```java
import ru.tehkode.permissions.bukkit.PermissionsEx;

if (!PermissionsEx.isAvailable()) {
    getLogger().warning("PermissionsExPlus is not available; integration disabled");
    return;
}
```

`isAvailable()` is `true` only when the plugin is enabled and its `PermissionManager` has started successfully.

## Get PermissionManager

### Static access

```java
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.exceptions.PermissionsNotAvailable;

try {
    PermissionManager manager = PermissionsEx.getPermissionManager();
    // use manager
} catch (PermissionsNotAvailable e) {
    getLogger().warning("PermissionsExPlus is not ready");
}
```

Prefer checking `isAvailable()` before calling `getPermissionManager()`.

### Services manager

PermissionsExPlus registers `PermissionManager` with Bukkit services:

```java
import org.bukkit.Bukkit;
import ru.tehkode.permissions.PermissionManager;

PermissionManager manager = Bukkit.getServicesManager()
    .load(PermissionManager.class);

if (manager == null) {
    getLogger().warning("PermissionsExPlus is not registered");
    return;
}
```

## Typical onEnable pattern

```java
@Override
public void onEnable() {
    if (!PermissionsEx.isAvailable()) {
        getLogger().info("PermissionsExPlus not found; skipping integration");
        return;
    }

    PermissionManager pex = PermissionsEx.getPermissionManager();
    // register listeners, hooks, etc.
}
```

## Optional: hard depend

If your plugin requires PermissionsExPlus:

```yaml
depend: [PermissionsEx]
```

Your plugin will not enable unless PermissionsExPlus loads first. You can still call `PermissionsEx.isAvailable()` to handle failed backend startup.

Next: [User lookup](user-lookup) or [Groups](groups).
