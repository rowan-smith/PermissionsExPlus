# API roadmap and gaps

Policy: **legacy `ru.tehkode.*` stays frozen**. New capabilities belong under `dev.rono.permissions.api.*`.

---

## Implemented (modern API)

| Feature | API |
|---------|-----|
| Modern event bus | `pex.events()` → `PexPermissionEventBus` |
| Player checks (Bukkit) | `PexBukkitPermissions.on(player).hasPermission(...)` |
| Global permission checks | `pex.user(id).hasPermission("node")` |
| Promote / demote | `PexUser.promote` / `PexUser.demote` (+ `PexRankingException`) |
| Backend admin | `pex.backend().activate`, `createHandle`, `importFrom` |
| Import / export | `pex.backend().exportData`, `importData(document, PexImportMode)` |
| Hierarchy helpers | `PexGroup.children` / `PexGroup.descendants`, `PexGroup.members(world, inherit)` |
| Flat API | `pex.user()`, `pex.world()`, `pex.users()`, `pex.groups()`, `pex.backend()` |
| Batch edits | `pex.session().start()` → `PexPermissionEditSession` |
| Async reload | `pex.reloadAsync()` → `CompletableFuture<Void>` |
| Proxy `PexPermissionService` | `PermissionsExPlus` on Bungee/Waterfall |

---

## Still open

### Permission check diagnostics

Legacy: `getMatchingExpression`, `explainExpression`.

**Proposed:** `PermissionCheckResult` record on `PexPermissionSubject` or `PexPermissionService`.

### Configuration read surface

Legacy: `PermissionManager.getConfiguration()`.

**Proposed:** immutable `PexPermissionService.configuration()` snapshot.

### Regex / matcher access

Legacy: `getPermissionMatcher()`.

**Proposed:** read-only `PexPermissionService.matcher()`.

### PexUser cache control

Legacy: `resetUser`, `clearUserCache`, `cacheUser`.

**Proposed:** same operations on modern `PexPermissionService`.

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
