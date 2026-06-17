---
title: Real-Server Test Matrix
description: Pre-release verification checklist for game servers and proxies.
slug: /developers/testing-matrix
---
Use this checklist before tagging a release.

## Game server (Paper recommended)

| Component | Version | Notes |
|-----------|---------|-------|
| Minecraft | 1.21.11 | Primary compile/test API |
| Java | 21+ | Required for current artifacts |
| PermissionsExPlus | `PermissionsExPlus-%%site.version%%.jar` from `universal/target/` | Remove older PEX jars first |

### Plugins to install alongside PEX

| Plugin | Purpose |
|--------|---------|
| Vault | Permission/chat bridge smoke test |
| EssentialsX | Common `has()` consumer |

### Scenarios

1. Clean start — default `h2` backend creates `permissions.mv.db` (imports `permissions.yml` if present)
2. `/pex reload` — no duplicate attachments; permissions still resolve
3. `/pex user <player> check <node>` — matches superperms bridge
4. Group prefix/suffix visible to Vault/chat if installed
5. Join/quit — user cache reset without errors

## Legacy range spot checks

| Minecraft | Java | Expected |
|-----------|------|----------|
| 1.8.8 | 21 | Loads with warning if outside tested build; verify `/pex` and one permission node |
| 1.12.2 | 21 | Same |
| 1.21.11 | 21 | Full feature path |

## Proxy (optional)

| Component | Version |
|-----------|---------|
| Waterfall or BungeeCord | Latest stable for your network |
| PermissionsExPlus universal jar | Same build as backends |

Verify `/pex` on proxy and permission checks for connected players.

## Regression artifacts

- `example-legacy-plugin` JAR — compiles only against legacy API + stub (`example-legacy-plugin/src/test/`)
- `example-plugin` JAR — compiles only against modern API (`example-plugin/src/test/`)
- API docs: [Hook Plugin API](/developers/api)
- Optional: drop classic hook plugin JARs into `legacy-compat/src/test/resources/plugin-jars/` and run `mvn -pl legacy-compat test`

## Automated test coverage

Run the full suite from the repo root:

```bash
mvn test
```

| Module | What is tested |
|--------|----------------|
| `api-core` | `PermissionContext`, `PermissionAddRequest`, bus dispatch types |
| `platform-api` | `PlatformDescriptor`, scheduler, event bus, context resolver, legacy hook bytecode probe |
| `legacy-api` | `LegacyApiContractTest`, event compatibility, `DateUtils` |
| `common` | Engine, backends, commands, modern API integration, legacy compatibility |
| `bukkit` | Spigot platform bridge, event publisher, backends, MockBukkit smoke |
| `bungee` | Platform adapter, permission bridge, legacy hook detection |
| `velocity` | Platform adapter, legacy hook detection |
| `sponge` | Platform adapter, legacy hook detection |
| `proxy-common` | Proxy config bridge, legacy bridge controller, file backend |
| `universal` | Bootstrap artifact wiring and shaded jar contents (requires `mvn package -pl universal -am` first) |
| `example-plugin` / `example-legacy-plugin` | Hook plugin compile contract |
| `legacy-compat` | Modern hook smoke test, optional classic plugin JAR probes |

Platform adapter and universal jar tests were added alongside the flat module layout. See [Architecture — Testing](/developers/architecture#testing) for the module map.
