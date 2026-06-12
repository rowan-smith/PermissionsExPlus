# PermissionsExPlus hook plugin API

PermissionsExPlus exposes **two compile surfaces** for companion plugins. Both talk to the same runtime manager on game servers.

| Surface | Maven artifact(s) | Package | Status |
|---------|-------------------|---------|--------|
| **Modern** | `permissionsex-api` | `dev.rono.permissions.api.*` | Active — new features land here |
| **Legacy (classic)** | `permissionsex-legacy-api`, optional `permissionsex-legacy-stub` | `ru.tehkode.permissions.*` | Frozen — baseline commit `628215f` |

## Which API should I use?

| Situation | Use |
|-----------|-----|
| New plugin | [Modern API](MODERN_API.md) (`PermissionService`) |
| Existing PEX 1.23.x hook plugin | [Legacy API](LEGACY_API.md) — no recompile required for typical hooks |
| Static `PermissionsEx.getUser(...)` calls | Legacy API + `permissionsex-legacy-stub` |
| Permission change events (modern) | `pex.events()` or legacy Bukkit events on Spigot |
| Proxy (Bungee/Waterfall) | `ProxyPermissionServices.permissionService()` (+ legacy `PermissionManager`) |

## Documentation

| Document | Contents |
|----------|----------|
| [MODERN_API.md](MODERN_API.md) | `PermissionService`, `User`, `Group`, world contexts, timed permissions, Maven setup, examples |
| [LEGACY_API.md](LEGACY_API.md) | `PermissionManager`, `PermissionUser`, `PermissionGroup`, `PermissionsEx` stub, events, utils |
| [FUTURE.md](FUTURE.md) | Recommended additions and known gaps |

## Runtime registration (Spigot/Paper)

Both APIs resolve to the **same object** (`DefaultPermissionManager`):

```java
// Modern
RegisteredServiceProvider<PermissionService> modern =
        getServer().getServicesManager().getRegistration(PermissionService.class);

// Legacy
RegisteredServiceProvider<PermissionManager> legacy =
        getServer().getServicesManager().getRegistration(PermissionManager.class);

// modern.getProvider() == legacy.getProvider()  (same instance)
```

## Sample plugins

| Module | API |
|--------|-----|
| [`example-plugin/`](../../example-plugin/) | Modern only |
| [`example-legacy-plugin/`](../../example-legacy-plugin/) | Legacy + stub |

## Related docs

- [COMPATIBILITY.md](../COMPATIBILITY.md) — Minecraft/Java version matrix
- [ARCHITECTURE.md](../../ARCHITECTURE.md) — module layout and namespace policy
- [Hook plugin development (README)](../../README.md#hook-plugin-development) — Maven coordinates and build notes
