# API roadmap and gaps

Policy: **legacy `ru.tehkode.*` stays frozen**. New capabilities belong under `dev.rono.permissions.api.*`.

---

## Implemented (modern API)

| Feature | API |
|---------|-----|
| Modern event bus | `PermissionService.events()` → `PermissionEventBus` |
| Player checks (Bukkit) | `permissionsex-api-bukkit` → `BukkitPermissions.has(...)` |
| Promote / demote | `User.promote` / `User.demote` (+ `RankingException`) |
| Backend admin | `setActiveBackend`, `createBackendHandle`, `importFromBackend` |
| Import / export | `exportData`, `importData(document, ImportMode)` |
| Hierarchy helpers | `Group.children` / `Group.descendants`, `Group.members(world, inherit)` |
| Fluent API | `user()` / `findUser()` / `group()` / `findGroup()` / `world()` / `findWorld()` |
| Batch edits | `openEditSession()` → `PermissionEditSession` |
| Async reload | `reloadAsync()` → `CompletableFuture<Void>` |
| Proxy `PermissionService` | `ProxyPermissionServices` on Bungee/Waterfall |

---

## Still open

### Permission check diagnostics

Legacy: `getMatchingExpression`, `explainExpression`.

**Proposed:** `PermissionCheckResult` record on `PermissionSubject` or `PermissionService`.

### Configuration read surface

Legacy: `PermissionManager.getConfiguration()`.

**Proposed:** immutable `PermissionService.configuration()` snapshot.

### Regex / matcher access

Legacy: `getPermissionMatcher()`.

**Proposed:** read-only `PermissionService.matcher()`.

### User cache control

Legacy: `resetUser`, `clearUserCache`, `cacheUser`.

**Proposed:** same operations on modern `PermissionService`.

### Superperms bridge introspection

Spigot-only attachment/injection status for support plugins.

### Observability

Metrics hooks, structured logging adapters for large networks.

---

## Contributing

When adding modern API methods:

1. Types in `permissionsex-api` (optional Bukkit/proxy helpers in separate modules).
2. Implementation in `dev.rono.permissions.core.api.*`.
3. Tests in `ModernPermissionServiceTest` or focused test classes.
4. Update [MODERN_API.md](MODERN_API.md) and this file.
