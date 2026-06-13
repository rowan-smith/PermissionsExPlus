---
title: Hook Plugin API
description: Modern and legacy API surfaces for PermissionsExPlus companion plugins.
slug: /developers/api
---
PermissionsExPlus exposes **two compile surfaces** for companion plugins. Both talk to the same runtime manager on game servers.

| Surface | Maven artifact(s) | Package | Status |
|---------|-------------------|---------|--------|
| **Modern** | `permissionsex-api` | `dev.rono.permissions.api.*` | Active — new features land here |
| **Legacy (classic)** | `permissionsex-legacy-api`, optional `permissionsex-legacy-stub` | `ru.tehkode.permissions.*` | Frozen — baseline commit `628215f` |

## Which API should I use?

| Situation | Use |
|-----------|-----|
| New plugin | [Modern API](/developers/api/modern) (`PermissionsEx.getApi()`) |
| Existing PEX 1.23.x hook plugin | [Legacy API](/developers/api/legacy) — no recompile required for typical hooks |
| Static `PermissionsEx.getUser(...)` calls | Legacy API + `permissionsex-legacy-stub` |
| Static modern entry | `permissionsex-api` + `PermissionsEx.getApi()` (`PermissionsExApi`) |
| Permission change events (modern) | `pex.events()` or legacy Bukkit events on Spigot |
| Proxy (Bungee/Waterfall) | `PermissionsEx.getApi()` (+ deprecated classic `PermissionManager` methods) |

## Documentation

| Document | Contents |
|----------|----------|
| [Modern API](/developers/api/modern) | `PermissionsEx.getApi()`, managers, holder permissions, managers, subjects, world contexts |
| [Legacy API](/developers/api/legacy) | `PermissionManager`, `PermissionUser`, `PermissionGroup`, `PermissionsEx` stub, events, utils |
| [API Invariants](/developers/api/invariants) | Primary API layering, subject role split, timed expiry, world-context rules |
| [API Roadmap](/developers/api/future) | Recommended additions and known gaps |

## Runtime registration (Spigot/Paper)

Both APIs resolve to the **same object** (`DefaultPermissionManager`):

```java
// Modern (managers + holder permissions)
PermissionsExApi api = PermissionsEx.getApi();

// Classic manager
PermissionManager manager = api.getPermissionManager();

// Deprecated static alias
PermissionManager legacy = PermissionsEx.getPermissionManager();
```

## Sample plugins

| Module | API |
|--------|-----|
| [`plugin/permissionsex-example-plugin/`](https://github.com/%%site.repo%%/tree/main/plugin/permissionsex-example-plugin/) | Modern only |
| [`plugin/permissionsex-example-legacy-plugin/`](https://github.com/%%site.repo%%/tree/main/plugin/permissionsex-example-legacy-plugin/) | Legacy + stub |

## Tests

Modern API integration tests live in `common/permissionsex-core/src/test/java/dev/rono/permissions/core/`:

| Class | Coverage |
|-------|----------|
| `ModernApiManagerLifecycleTest` | find/get/create/exists, exceptions, counts |
| `ModernApiSubjectPermissionsTest` | permissions, negation, meta, timed grants |
| `ModernApiWorldContextTest` | `inContext` facades, global vs realm scope |
| `ModernApiGroupHierarchyTest` | membership graph, parent trees, identifiers |
| `ModernApiHolderPermissionTest` | holder checks, `PermissionAddRequest` |
| `ModernApiEventBusTest` | subscribe/unsubscribe, dispatches |
| `ModernApiLadderTest` | promote/demote, rank metadata |
| `ApiLayerInvariantTest` | architectural composition guards |
| `WorldsTest`, `PermissionContextTest` | pure utility unit tests |

Run: `mvn -pl common/permissionsex-core test -Dtest='ModernApi*Test,ApiLayerInvariantTest,WorldsTest,PermissionContextTest'`

## Related docs

- [Platform Compatibility](/developers/compatibility) — Minecraft/Java version matrix
- [Architecture](/developers/architecture) — module layout and namespace policy
- [Contributing](/developers/contributing) — Maven coordinates and build notes
