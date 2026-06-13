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
| PermissionsExPlus | `PermissionsExPlus-%%site.version%%.jar` from `bootstrap/target/` | Remove older PEX jars first |

### Plugins to install alongside PEX

| Plugin | Purpose |
|--------|---------|
| Vault | Permission/chat bridge smoke test |
| EssentialsX | Common `has()` consumer |

### Scenarios

1. Clean start — default `file` backend creates `permissions.yml`
2. `/pex reload` — no duplicate attachments; permissions still resolve
3. `` `/pex user <player> check <node>` `` — matches superperms bridge
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

- `example-legacy-plugin` JAR — compiles only against legacy API + stub
- `example-plugin` JAR — compiles only against modern API
- API docs: [Hook Plugin API](/developers/api)
- Optional: drop classic hook plugin JARs into `legacy-api/permissionsex-legacy-compat/src/test/resources/plugin-jars/` and run `mvn -pl legacy-api/permissionsex-legacy-compat test`
