<p align="center">
  <img src="website/static/img/logo.svg" alt="PermissionsExPlus Logo" width="800">
</p>

PermissionsExPlus is a modern Bukkit permissions plugin with Spigot and Paper adapters. Core and both platform adapters are implemented against API.

## Modules

Public Contract:
- `PermissionsExPlusApi` — public API contracts, contextual nodes, modifiers, events, and resolvers.

Implementation is split into several modules:
- `PermissionsExPlus/Core` — platform-independent API runtime, storage, commands, caching, configuration, auditing, and Redis sync.
- `PermissionsExPlus/Spigot` — Spigot plugin adapter.
- `PermissionsExPlus/Paper` — Paper plugin adapter.

Legacy Compatibility:
- `PermissionsExApiAdapter` — legacy PermissionsEx API/plugin compatibility delegates to `PermissionsExPlusApi`.
- `PermissionsExCommandAdapter` — restores legacy PermissionsEx commands.

## Build and installation

Build with Maven and place the appropriate shaded Spigot or Paper JAR in the server's `plugins/` directory:

```shell
mvn clean package
```

On first startup the plugin creates `config.yml`, `advanced.yml`, `database.yml`, and `data/` under `plugins/PermissionsExPlus/`.

## Configuration

All existing configuration surfaces are retained:

- `config.yml`: default group, wildcard/negation policy, hooks, and debug settings.
- `advanced.yml`: world/server/gamemode/proxy contexts, static environment contexts, inheritance depth, cache and expiry intervals, UUID source, auditing, and Redis messaging.
- `database.yml`: `h2`, `sqlite`, `mysql`, `postgres`, `memory`, `yaml`, or `json`, including connection-pool and credential settings.

API flat-file data is stored in `data/permissions.yml` or `data/permissions.json`. SQL backends use the `pex_data` table. Redis messages use the `permissionsexplus` protocol namespace.

## API

```java
PexApi api = PexProvider.get();

api.users().find(playerId).thenAccept(optional -> optional.ifPresent(user -> {
    PermissionResult result = api.resolvers().permissions()
            .check(user, "example.use", ContextKeys.WORLD, "survival");
}));
```

The facade also exposes `backend()` for storage discovery, `contexts()` for runtime
context calculators, `placeholders()` for cache-only placeholder resolution, and `events()`
for lifecycle and modification subscriptions.

Mutations use atomic modifiers and complete after persistence and cache replacement:

```java
api.users().modify(playerId, modifier -> modifier
        .allowPermission("example.use")
        .addGroup("member")
        .setPrefix("[Member]"));
```

## Commands

The command surface remains rooted at `/pex` and includes:

- user inspection, permission add/remove/check, group membership, and options;
- group create/delete, permission, and parent management;
- ladder create/delete, group ordering, promotion, and demotion;
- backend status, version, help, and reload.

The plural registry commands (`/pex users`, `/pex groups`, and `/pex ladders`), backend
listing/switch guidance, permission traces, contextual permission arguments, group cloning,
member management, option inspection, and ladder moves are retained. Context arguments use
flags such as `--world <world>` and `--server <server>`.

Each mutation uses the same API manager/modifier path as third-party consumers.

## Runtime behavior

Permission and option resolution supports contextual nodes, expiry, inheritance, contextual defaults, group weights, exact/wildcard precedence, explicit deny tie-breaking, primary groups, and explanation candidates. User/group/ladder snapshots are immutable. Mutations are persisted before their completion stage resolves and publish API events after cache replacement.

Redis publishes user invalidations for user changes and fan-out invalidations for group changes. Remote invalidations reload only users currently present in the local cache. Audit broadcasting and network-wide audit messages continue to use the configured Redis channel.

Metadata is represented natively as option nodes. Prefix, suffix, display name,
weight, and custom metadata use option nodes.

## Compatibility boundaries

The legacy adapter routes user/group permissions, options, parent groups, default groups,
weights, identifiers, and enumeration through API. Its world-inheritance configuration is
currently a process-local compatibility view; API context resolution remains authoritative.

The model intentionally does not reproduce the old command-extension
registry, or every fine-grained/cancellable v1 event type. Metadata translates to option
nodes, and publishes lifecycle plus aggregate
modification events. Existing v1 flat-file/SQL data also requires an explicit migration into
the store; startup does not silently rewrite it.
