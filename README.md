<p align="center">
  <img src="website/static/img/logo.svg" alt="PermissionsExPlus Logo" width="800">
</p>

PermissionsExPlus is a modern permissions plugin for Bukkit, Spigot, Paper, SpongeVanilla, Velocity and BungeeCord. Core and every platform plugin are implemented against the API.

## Supported versions

PermissionsExPlus supports the last version of every finished Minecraft release line and every
version of the current one (26.x). Each cell names the release jar to install, without its
`PermissionsExPlus-` prefix and `.jar` suffix; for example `Spigot-1.21.11-26.3` is
`PermissionsExPlus-Spigot-1.21.11-26.3.jar`.

| Minecraft | CraftBukkit / Crucible | Spigot | Paper | SpongeVanilla |
|---|---|---|---|---|
| 1.7.10 | `Bukkit-1.7.10` | — | — | — |
| 1.8.8 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | — |
| 1.9.4 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | — |
| 1.10.2 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | — |
| 1.11.2 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | — |
| 1.12.2 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | `Sponge-1.12.2` |
| 1.13.2 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | — |
| 1.14.4 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | — |
| 1.15.2 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | — |
| 1.16.5 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | `Sponge-1.16.5` |
| 1.17.1 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | `Sponge-1.17.1-1.19.4` |
| 1.18.2 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | `Sponge-1.17.1-1.19.4` |
| 1.19.4 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | `Sponge-1.17.1-1.19.4` |
| 1.20.6 | — | `Spigot-1.8.8-1.20.6` | `Spigot-1.8.8-1.20.6` | `Sponge-1.20.6-26.3` |
| 1.21.11 | — | `Spigot-1.21.11-26.3` | `Paper-1.21.11-26.3` | `Sponge-1.20.6-26.3` |
| 26.1 | — | `Spigot-1.21.11-26.3` | `Paper-1.21.11-26.3` | `Sponge-1.20.6-26.3` |
| 26.2 | — | `Spigot-1.21.11-26.3` | `Paper-1.21.11-26.3` | `Sponge-1.20.6-26.3` |
| 26.3 | — | `Spigot-1.21.11-26.3` | `Paper-1.21.11-26.3` | `Sponge-1.20.6-26.3` |

- **Velocity** and **BungeeCord** use `PermissionsExPlus-Velocity.jar` and
  `PermissionsExPlus-BungeeCord.jar` on their current releases.
- **26.1** is tested as 26.1.2, its latest patch.
- **SpongeVanilla:** Sponge has no release for 1.13.2 or 1.14.4, and its only 1.15.2 build does
  not start. Its 1.8.9 - 1.11.2 builds (Sponge API 4 - 6, betas only) are not supported.
- Other versions (for example 1.20.4 or 1.21.4) may work but are not supported or tested.
  When 27.x starts, 26.3 stays supported and 26.1 and 26.2 are dropped.

Every version in the grid is tested on a real server in CI for every change. See
[Requirements](docs/documentation/getting-started/requirements.md) for Java versions.

## Modules

Public Contract:
- `PermissionsExPlusApi` — public API contracts, contextual nodes, modifiers, events, and resolvers.

Implementation is split into several modules:
- `PermissionsExPlus/Core` — platform-independent API runtime, storage, commands, caching, configuration, auditing, and Redis sync.
- `PermissionsExPlus/Bukkit` — the Bukkit, Spigot and Paper plugins (`Common` is shared by all of them).
- `PermissionsExPlus/Sponge` — the SpongeVanilla plugins.
- `PermissionsExPlus/Proxy` — the Velocity and BungeeCord plugins.

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
context calculators, and `events()` for lifecycle and modification subscriptions.

Mutations use atomic modifiers and complete after persistence and cache replacement:

```java
api.users().modify(playerId, modifier -> modifier
        .allowPermission("example.use")
        .addGroup("member")
        .setPrefix("[Member]"));
```

On a database shared by several servers, a change that another server saved first is not
overwritten: the modification is applied again to the newly stored user or group, so the
modifier action may run more than once and should only describe the change.

## Commands

The command surface remains rooted at `/pex` and includes:

- user inspection, permission add/remove/check, group membership, and options;
- group create/delete, permission, and parent management;
- ladder create/delete, group ordering, promotion, and demotion;
- world inheritance (`/pex worlds`, `/pex world <world> inherit <worlds...>`);
- importing an original PermissionsEx installation (`/pex import permissionsex`);
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
weights, identifiers, world inheritance, timed group memberships, and enumeration through
API. World inheritance is stored in `worlds.yml` and applied to every query.

The model intentionally does not reproduce the old command-extension
registry, or every fine-grained/cancellable v1 event type. Metadata translates to option
nodes, and publishes lifecycle plus aggregate
modification events. Existing v1 flat-file/SQL data is imported explicitly with
`/pex import permissionsex`; startup does not silently rewrite it.
