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
| PermissionsEx 1.23.4 hook plugin | [Legacy API](/developers/api/legacy) — no recompile required for typical hooks |
| Static `PermissionsEx.getUser(...)` calls | Legacy API + `permissionsex-legacy-stub` |
| Static modern entry | `permissionsex-api` + `PermissionsEx.getApi()` (`PermissionsExApi`) |
| Permission change events (modern) | `api.getEventBus()` or legacy Bukkit events on Spigot |
| Proxy (Bungee/Waterfall) | `PermissionsEx.getApi()` (+ deprecated classic `PermissionManager` methods) |

## Documentation

| Document | Contents |
|----------|----------|
| [API Cookbook](/developers/cookbook) | Copy-paste recipes — context, realms, groups, timed grants, proxy |
| [MODERN_API.md](/developers/api/modern) | `PermissionsEx.getApi()`, managers, realms, holder permissions, subjects, contexts |
| [LEGACY_API.md](/developers/api/legacy) | `PermissionManager`, `PermissionUser`, `PermissionGroup`, `PermissionsEx` stub, events, utils |
| [API_INVARIANTS.md](/developers/api/invariants) | Primary API layering, subject role split, timed expiry, world-context rules |
| [FUTURE.md](/developers/api/future) | Recommended additions and known gaps |

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
| [`example-plugin/`](https://github.com/%%site.repo%%/tree/main/example-plugin/) | Modern only |
| [`example-legacy-plugin/`](https://github.com/%%site.repo%%/tree/main/example-legacy-plugin/) | Legacy + stub |

## Tests

### Modern API integration (`common/`)

Integration tests live in `common/src/test/java/dev/rono/permissions/core/`:

| Class | Coverage |
|-------|----------|
| `ModernApiManagerLifecycleTest` | find/get/create/exists, exceptions, counts |
| `ModernApiSubjectPermissionsTest` | permissions, negation, meta, timed grants |
| `ModernApiRealmTest` | realm registry, inheritance |
| `ModernApiWorldContextTest` | `inContext` facades, global vs realm scope |
| `ModernApiGroupHierarchyTest` | membership graph, parent trees, identifiers |
| `ModernApiHolderPermissionTest` | holder checks, `PermissionAddRequest` |
| `ModernApiEventBusTest` | subscribe/unsubscribe, dispatches |
| `ModernApiLadderTest` | promote/demote, rank metadata |
| `ApiLayerInvariantTest` | architectural composition guards |
| `RealmsTest`, `PermissionContextTest` | pure utility unit tests |

Run: `mvn -pl common test -Dtest='ModernApi*Test,ApiLayerInvariantTest,RealmsTest,PermissionContextTest'`

### Per-module unit tests

| Module | Examples |
|--------|----------|
| `api-core` | `PermissionAddRequestBuilderTest`, `PermissionContextApiCoreTest`, `PermissionDispatchTypesTest` |
| `platform-api` | `PlatformDescriptorTest`, `DirectPlatformSchedulerTest`, `NoOpPlatformEventBusTest` |
| `legacy-api` | `LegacyApiContractTest`, `LegacyEventCompatibilityTest` |
| `bukkit` | `SpigotPlatformBridgeTest`, `MockBukkitPermissionsExTest` |
| `bungee` | `BungeePlatformAdapterTest`, `BungeePexPermissionBridgeTest` |
| `velocity` | `VelocityPlatformAdapterTest` |
| `sponge` | `SpongePlatformAdapterTest` |
| `proxy-common` | `ProxyConfigBridgeTest`, `ProxyLegacyBridgeControllerTest` |
| `universal` | `BootstrapArtifactsTest`, `UniversalJarContentsTest` (requires packaged jar) |
| `example-plugin` / `example-legacy-plugin` | `ExamplePluginContractTest`, `ExampleLegacyPluginContractTest` |
| `legacy-compat` | `ModernHookPluginSmokeTest`, optional `LegacyClassicJarProbeTest` |

Full suite: `mvn test` from the repo root. See [Real-Server Test Matrix](/developers/testing-matrix) for manual pre-release checks.

The modern API is **Realm-only** in 3.0-SNAPSHOT: `RealmManager`, `Realm`, `Realms`, and `PermissionContext` replace the removed `World*` types. See the [Cookbook](/developers/cookbook) for world, server, and realm examples.

## Related docs

- [Platform Compatibility](/developers/compatibility) — Minecraft/Java version matrix
- [Developer Overview](/developers) — Maven coordinates and build notes
