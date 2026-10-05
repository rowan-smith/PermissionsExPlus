---
sidebar_position: 1
---

# Backends

PermissionsExPlus stores data in a pluggable backend. Built-in aliases: `file`, `sql`, `memory`, `multi`.

```java
PermissionManager manager = PermissionsEx.getPermissionManager();
PermissionBackend backend = manager.getBackend();
```

## Switch backend at runtime

```java
import ru.tehkode.permissions.exceptions.PermissionBackendException;

try {
    manager.setBackend("sql"); // name of backend section / type
} catch (PermissionBackendException e) {
    getLogger().severe("Failed to switch backend: " + e.getMessage());
}
```

`setBackend` clears caches, creates the new backend, preloads groups, and fires `BACKEND_CHANGED`.

## Create without activating

Useful for copying data between backends:

```java
PermissionBackend other = manager.createBackend("sql");
// transfer with backend APIs / admin tools, then setBackend when ready
```

Backend settings still come from `config.yml` (see [Storage Backends](/docs/next/configuration/storage)).

## Reload / reset

```java
try {
    manager.reset();          // reload current backend, fires RELOADED
    manager.reset(false);     // same without calling the system event
} catch (PermissionBackendException e) {
    getLogger().severe("Reload failed: " + e.getMessage());
}
```

See [Debug & matching](debug-and-matching) for `PermissionBackendException` and related errors.
