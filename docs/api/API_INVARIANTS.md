# API invariants and layering

This document codifies the permission API architecture: which entry points exist, how they relate, and the invariants that keep the system stable as it evolves.

Policy: **new hook plugins use `PermissionsExApi`**. Legacy `ru.tehkode.*` stays frozen. See [README.md](README.md) and [ARCHITECTURE.md](../../ARCHITECTURE.md).

---

## Primary API

| Layer | Type | Role |
|-------|------|------|
| **Primary** | `PermissionsExApi` | Entry point for new plugins — managers, events, holder bridge accessor |
| **Convenience** | `User` / `Group` (`PermissionSubject`) | Subject-centric CRUD and checks obtained from managers |
| **Legacy bridge** | `PermissionManager` | Frozen classic API + holder-based operations for interop |
| **Internal engine** | `DefaultPermissionManager`, `PermissionEntity`, `HierarchyTraverser` | Resolution, persistence, caches — not for hook plugins |

```java
// Correct for new plugins
var api = PermissionsEx.getApi();
var user = api.getUserManager().getUser(player.getUniqueId());
if (user.has("my.plugin.use")) { ... }

// Holder + rich context (still via primary API)
api.getPermissionManager().hasPermission(
    user.asHolder(), "my.plugin.use", PermissionContext.of(world, server, region, gamemode));

// Legacy interop only — do not use in new code
PermissionManager classic = api.getPermissionManager();
PermissionUser legacyUser = classic.getUser(player.getName());
```

All three paths above resolve to the **same in-memory entities** in `DefaultPermissionManager`. The difference is surface area and intent, not data.

---

## Core invariants

These rules must hold across refactors:

| Invariant | Meaning |
|-----------|---------|
| **Mutation through managers** | Create/load subjects via `UserManager` / `GroupManager`. Persist with `subject.save()`. Do not construct engine entities from hook plugins. |
| **Evaluation through subject / engine** | Permission checks and effective lists delegate to `PermissionEntity` + `HierarchyTraverser`. API adapters must not reimplement resolution. |
| **Context facade = thin projection** | `SubjectContext` binds a `PermissionContext` and delegates to `PermissionSubject`. No business logic in `SubjectContexts`. |
| **Unified expiry** | Timed permissions and timed group memberships use different storage but share one scheduler: `TimedExpiryCoordinator`. |

Violating these produces drift bugs (especially around timed expiry and inheritance).

---

## Three overlapping entry points (by design)

```
PermissionsExApi  ──►  managers  ──►  UserImpl / GroupImpl  ──►  PermissionEntity
       │
       └──► getPermissionManager()  ──►  HolderPermissionService  ──►  PermissionEntity
```

| Entry | When to use |
|-------|-------------|
| `api.getUserManager().getUser(uuid)` | Normal subject operations |
| `user.inContext(PermissionContext.world("world")).addPermission(...)` | Context-bound convenience (thin facade) |
| `user.inContext(PermissionContext.server("lobby")).addPermission(...)` | Server-bound convenience on proxies |
| `api.getPermissionManager().hasPermission(holder, node, context)` | Rich `PermissionContext` checks or holder-centric plugins |
| `manager.getUser(name)` | Legacy plugins only |

Long-term, only the first row is “correct” for new code. The others remain for compatibility and specialized holder/context flows.

---

## PermissionSubject role split

The public type `PermissionSubject` is large by necessity (users and groups share one surface). Internally it composes three narrower roles:

| Role | Interface | Responsibility |
|------|-----------|----------------|
| Identity | `SubjectIdentity` | `type`, `identifier`, `name`, `virtual` |
| Evaluation | `PermissionView` | `has`, effective/direct lists, prefix/suffix/options reads, timed reads |
| Mutation | `PermissionMutator` | add/remove/set permissions, meta writes, `save`, `delete` |

Implementations (`AbstractPermissionSubjectAdapter`) should treat evaluation and mutation as separate concerns even though callers see one interface. Tests and mocks can depend on `PermissionView` alone when writes are not needed.

`SubjectContext` is the context-bound projection of `PermissionView` + `PermissionMutator` methods (still pure delegation).

---

## Timed permissions and timed groups

Two parallel **grant models**, one **expiry engine**:

| System | Storage | Purpose |
|--------|---------|---------|
| Timed permissions | `AbstractPermissionEntity.timedPermissions` + epoch map | Direct temporary grants |
| Timed groups | User option `group-{name}-until` | Temporary structural inheritance |
| Holder add result | `PermissionNode.expiresAt()` | Write receipt after holder-based add |

Read models for hook plugins:

- `TimedPermissionEntry` — permission + context + remaining seconds
- `TimedGroupMembership` — group + context + remaining seconds

**Rule:** registering or sweeping expiry always goes through `TimedExpiryCoordinator.notifyEarliestExpiry` / `runSweep`. New timed features must hook into that path, not spawn separate timers.

---

## Context facade constraints

`SubjectContexts` is intentionally repetitive: each method forwards `subject.method(boundContext)`.

Allowed:

- Context binding via `PermissionContext`
- Binding `context()` on the returned facade
- Type-specific wrappers (`UserContext`, `GroupContext`) that add group/member operations

Not allowed:

- Permission resolution or inheritance logic
- Timed expiry calculations
- Backend I/O beyond what the subject already performs
- Caching effective results at the context layer

---

## Engine layer (not `PermissionService`)

There is no public `PermissionService` type today. The engine bridge consists of:

| Component | Role |
|-----------|------|
| `DefaultPermissionManager` | Central cache, backend, resolution entry |
| `InternalPermissionManager` | Runtime hooks (platform, events, timer) |
| `HolderPermissionService` | Holder → entity bridge |
| `AbstractPermissionEntity` | Storage + `has()` entry |
| `HierarchyTraverser` | World → inheritance → global DFS |
| `TimedExpiryCoordinator` | Unified timed expiry |

Future read-only configuration/matcher surfaces may adopt the `PermissionService` name (see [FUTURE.md](FUTURE.md)); until then, refer to these types in implementation docs.

---

## Contributing checklist

When adding API or engine features:

1. New public types → `permissionsex-api` under `dev.rono.permissions.api.*`
2. Implementation → `dev.rono.permissions.core.api.*` or core engine
3. Preserve the four invariants above
4. Route timed expiry through `TimedExpiryCoordinator`
5. Keep `SubjectWorldContexts` as delegation-only
6. Route group graph traversal through {@code GroupHierarchyEngine} (in {@code core.api.pex}) or {@code DefaultPermissionManager}
7. Update [MODERN_API.md](MODERN_API.md) and this file when behavior or layering changes

---

## Identifier vs entity APIs

Modern subject APIs follow a naming convention (not always enforced by types alone):

| Returns | Examples | Use when |
|---------|----------|----------|
| **Identifiers** (`String`) | `User.groups()`, `Group.memberIdentifiers()`, `Group.childIdentifiers()`, `Group.parents()` | Bulk listing, logging, storage keys |
| **Entities** (`User`, `Group`) | `Group.members()`, `Group.children()` | Mutations, full subject inspection |

`User.groups()` returns identifiers; `Group.members()` returns `User` adapters. This asymmetry is intentional: users are cheap to name-list; groups are often queried for member objects. When you need group objects from a user’s group list, resolve via `GroupManager.getGroup(name)`.

---

## Hierarchy traversal (internal)

Multiple public methods expose graph walks with an `inherit` flag:

| API | Direction | `inherit = false` | `inherit = true` |
|-----|-----------|-------------------|------------------|
| `User.groups(world, inherit)` | user → groups | direct assignments | transitive parents |
| `Group.members(world, inherit)` | group → users | direct members | members of descendant groups |
| `Group.children(world, inherit)` | group → groups | direct children | all descendants |
| `Group.parentTree(world)` | group → parents | — | transitive parents (always expanded) |

**Canonical engine:** `GroupHierarchyEngine` + `DefaultPermissionManager.getUsers` / `getGroups`. API adapters must not reimplement BFS/DFS. The `inherit` flag uses the same semantics everywhere (direct edge vs transitive closure).

Permission *resolution* (inheritance of permissions/options) uses `HierarchyTraverser` separately — do not conflate group-membership graph walks with permission inheritance walks.

---

## Scoping model

| System | Where | Primary use |
|--------|-------|-------------|
| **`PermissionContext`** | `PermissionSubject`, `User`, `Group`, holder `hasPermission` | Platform-neutral scope (modern API) |
| **`Map<String, String>` context** | Legacy holder `hasPermission(holder, node, map)` | Holder bridge interop; converted via `PermissionContext.fromMap` |

Rules:

- Modern subject APIs: pass `PermissionContext.global()` (or parameterless overloads) for global scope.
- Holder APIs: use `PermissionContext.of(...)` or `PermissionContext.fromMap(...)`; empty map = global.
- Do not mix raw maps on subject APIs — use `PermissionContext` only.

---

## Rank ladder dual model

| Layer | Types | Role |
|-------|-------|------|
| **State** | `Group.rank()`, `Group.rankLadder()`, `Group.setRank()` | Mutable rank metadata stored on the group |
| **Control plane** | `LadderManager.promote/demote/rank` | Sole authority for rank *transitions* |

Groups hold rank values; ladders orchestrate movement. Direct `setRank` edits metadata without validation — prefer `LadderManager` for player-facing promote/demote. Engine must not allow a group edit to silently corrupt ladder ordering without going through ladder operations.

---

## WorldManager: logical vs runtime worlds

`WorldManager.count()` includes backend world-inheritance nodes that may not map to a loaded Bukkit/Proxy dimension. **World** in PEX is a permission namespace, not strictly “loaded world.” On proxies, realm names are **backend server ids** — use `PermissionContext.server(id)` in proxy plugins.

---

## Cache, reset, and remove semantics

Legacy `PermissionManager` exposes many reset variants. Map them to three concepts:

| Concept | Operations | Effect |
|---------|------------|--------|
| **Remove data** | `subject.delete()`, `removePermission`, backend delete | Persisted data gone |
| **Invalidate cache** | `clearUserCache` | Keeps in-memory entity; clears resolved permission/meta caches |
| **Evict + reload** | `resetUser`, `resetGroup` | Drops in-memory entity; next access reloads from backend |
| **System reset** | `reset()` / `/pex reload` | Clears all entities, reloads backend |

Modern plugins should use `User.delete()` / `Group.delete()` and manager `find`/`get` — not `resetUser` unless integrating with legacy cache control.

---

## Threading expectations

| API area | Expectation |
|----------|-------------|
| `UserManager` / `GroupManager` / subjects | Assume **main thread** on Bukkit unless documented otherwise |
| `PermissionEventBus` | Subscribe/unsubscribe thread-safe; callbacks on publisher thread |
| `cacheUser` (legacy) | Thread-safe pre-materialization during async login |
| Backend I/O | May block; avoid on async threads without platform guarantees |

Prefer documenting threading in Javadoc over ad-hoc annotations until a standard annotation module exists.

---

## Holder permission adds

**Preferred:** `PermissionAddRequest.builder()...build()` → `addPermission(request)` — carries world context, expiry, and source.

Shorter overloads (`addPermission(holder, node)`, `addPermission(holder, node, duration)`) are convenience wrappers for global/permanent or simple timed grants. New holder-based features should extend the request object first.

---

## Null and empty semantics

| Context | `null` / empty meaning |
|---------|------------------------|
| Modern `PermissionContext` | `PermissionContext.global()` or parameterless overloads |
| Legacy `PermissionManager.getUser(name)` | {@code null} = not found (classic API) |
| Modern `UserManager.findUser` | {@code Optional.empty()} = not found |
| `option(key, world)` | {@code null} value = unset |
| `PermissionContext` map | empty = global scope |
| Method parameters | `@NonNull` by convention unless Javadoc says optional |

---

## Collection return types

Modern API methods return **immutable snapshots** (`List.copyOf`, `Set.copyOf`, `Map.copyOf`) at the adapter layer. They are not live views — mutations to returned collections do not affect stored permissions. Legacy `PermissionManager` sets may still be live engine collections; treat modern returns as read-only copies.

---

## Event bus guarantees

See {@link dev.rono.permissions.api.event.PermissionEventBus} Javadoc:

- Synchronous dispatch on publisher thread
- Registration-order listener invocation
- Non-cancellable informational dispatches
- Unsubscribe via `Subscription` token on plugin disable

---

## Known asymmetries and future work

Documented gaps intentionally left for backward compatibility:

- `PermissionManager` overload explosion and null-world holder bridge
- Context maps vs typed `PermissionScope`
- Legacy `getGroup(name)` returning null vs modern `Optional`/`find`
- `getActiveUsers()` live-set semantics on legacy manager vs snapshot lists on modern API

Track API improvements in [FUTURE.md](FUTURE.md).

