---
sidebar_position: 2
---

# Debug & matching

## Debug mode

```java
manager.setDebug(true);
boolean on = manager.isDebug();

// Per entity (ORed with manager debug)
user.setDebug(true);
group.setDebug(false);
```

When debug is on, permission checks log matching expressions to the server log. Toggling manager debug fires `DEBUGMODE_TOGGLE`.

Admins can also use `/pex toggle debug`.

## Permission matching

Nodes support wildcards and optional raw regex. Default matcher behaviour:

| Expression | Meaning |
|------------|---------|
| `essentials.fly` | Exact node |
| `essentials.*` | Prefix wildcard (`*` → match remainder) |
| `*` | Everything |
| `#essentials.fly` | Non-inheritable form of the node (`#` stripped for matching) |
| `$^essentials\..+$` | Raw regex when the expression starts with `$` |

```java
PermissionMatcher matcher = manager.getPermissionMatcher();
boolean matches = matcher.isMatches("essentials.*", "essentials.fly");

String expr = user.getMatchingExpression("essentials.fly", worldName);
boolean allowed = user.explainExpression(expr); // false if expression starts with "-"
```

You can replace the matcher with `manager.setPermissionMatcher(...)`, but most plugins should leave the default alone.

## Exceptions

| Exception | Typical cause |
|-----------|----------------|
| `PermissionsNotAvailable` | `getPermissionManager()` before PEX is ready |
| `PermissionBackendException` | Backend init / switch / reset failure |
| `RankingException` | Promote/demote not allowed |

```java
if (!PermissionsEx.isAvailable()) {
    return;
}
try {
    manager.setBackend("file");
} catch (PermissionBackendException e) {
    getLogger().log(Level.SEVERE, "Backend error", e);
}
```
