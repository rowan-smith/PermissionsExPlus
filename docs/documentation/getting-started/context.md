---
sidebar_position: 6
---

# Context

Contexts allow permissions to be active only under certain conditions.

## Default Contexts

- `world`: The name of the world the player is in.
- `server`: The identifier of the server (useful in Proxies).
- `proxy`: The identifier of the proxy server.
- `gamemode`: The player's current game mode.

## Using Contexts in Commands

Contexts are specified using flags such as `--world <world>` and `--server <server>`. You can provide multiple flags to require multiple contexts; when multiple contexts are provided, they act as an **AND** operation (all must match).

Example:
```bash
/pex user Rowan permission add example.permission --world survival --server main
```

## Using Contexts in Code

The `ContextKeys` enum provides type-safe constants for the default context keys:

```java
import dev.rono.permissions.api.context.ContextKeys;

// Single context
boolean allowed = api.resolvers().permissions()
        .hasPermission(user, "myplugin.use", ContextKeys.WORLD, "nether");

// QueryOptions with context
var query = QueryOptions.builder()
        .contexts(ContextKeys.WORLD, "survival")
        .build();

// Multiple contexts using ContextSet builder
var contexts = ContextSet.builder()
        .add(ContextKeys.WORLD, "arena")
        .add(ContextKeys.GAMEMODE, "CREATIVE")
        .build();
```

## Context Resolution

The API uses `ContextSet` and `QueryOptions` to resolve permissions against the current runtime state.
