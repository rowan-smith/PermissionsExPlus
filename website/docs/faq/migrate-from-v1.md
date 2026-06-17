---
title: Migrating from PermissionsEx 1.23.4
description: Upgrade from PermissionsEx 1.23.4 to PermissionsExPlus 3.0.0-SNAPSHOT — full backwards compatibility for data, commands, and hook plugins.
slug: /faq/migrate-from-v1
---

PermissionsExPlus **3.0.0-SNAPSHOT** is the first release line of this fork. The supported upgrade path is from the last upstream **PermissionsEx 1.23.4** release to **PermissionsExPlus 3.0.0-SNAPSHOT** (then `3.0.0` stable when published).

If you are on **PermissionsEx 1.23.4**, you can upgrade in place. No permission data rewrite is required.

> **Not on PermissionsEx yet?** See [Migrate from other plugins](/faq/migration) for LuckPerms, GroupManager, and similar.

---

## Version numbering

| Project | Last release | Next release | Notes |
|---------|--------------|--------------|-------|
| **PermissionsEx** (upstream) | **`1.23.4`** | — | Original project; unmaintained |
| **PermissionsExPlus** (this fork) | — | **`3.0.0-SNAPSHOT`** → `3.0.0` | New major line; not a `1.24` or `2.0` continuation |

The jump **`PermissionsEx 1.23.4` → `PermissionsExPlus 3.0.0-SNAPSHOT`** is intentional. PermissionsExPlus resets the major version to mark a new maintained chapter — not a missing upstream release.

---

## Backwards compatibility promise

PermissionsExPlus 3.0.0 is a **new start**, not a breaking rewrite. The following carry over from **PermissionsEx 1.23.4** without conversion:

| Area | Compatible? | Notes |
|------|-------------|-------|
| H2 database (`permissions.mv.db`) | **Yes** | Drop in the new jar |
| `permissions.yml` import / migration | **Yes** | Auto-import on first startup still works |
| SQL backends | **Yes** | Same JDBC config |
| Permission nodes, groups, inheritance | **Yes** | Same resolution rules |
| Classic `/pex` commands | **Yes** | `command-framework: classic` |
| Modern `/pex` commands | **Yes** | Default framework |
| Legacy `ru.tehkode.*` hook plugins | **Yes** | Frozen API, still supported |
| Modern `dev.rono.*` hook plugins | **Yes** | Active API surface |
| Vault consumers | **Yes** | Unchanged integration |

What changes is the **project name**, **version label**, **documentation focus**, and **new PermissionsExPlus features** — not your existing permission data.

---

## What is new in PermissionsExPlus 3.0

### Philosophy

- **New major line** — `3.0.0-SNAPSHOT` is the development baseline; `3.0.0` will be the first stable PermissionsExPlus release
- **Backwards compatible by default** — PermissionsEx 1.23.4 servers upgrade without data migration
- **Modern-first** — structured commands, export/import workflows, and `dev.rono.permissions.api` are the documented path forward
- **Legacy preserved** — classic commands and `ru.tehkode.*` hooks remain for existing ecosystems

### Defaults (PermissionsEx 1.23.4 vs PermissionsExPlus 3.0.0-SNAPSHOT)

| Area | PermissionsEx `1.23.4` | PermissionsExPlus `3.0.0-SNAPSHOT` |
|------|------------------------|-------------------------------------|
| Server platforms | **Bukkit / Spigot / Paper only** | Bukkit/Paper + **BungeeCord, Waterfall, Velocity, Sponge** |
| Storage backend | **`file`** (YAML) typical | **`h2` (H2)** default |
| Command framework | Classic only | **`modern`** default (+ classic) |
| YAML day-to-day storage | Common | **Import/migration** into H2 |
| Java runtime | **Java 7+** bytecode (Java 8+ typical in practice) | **Java 21+** required |
| Minecraft versions | **1.8.x era** (upstream built against Bukkit `1.8.8`) | **1.8.8 – 1.26.1** (explicit target) |

### New in PermissionsExPlus 3.0

- Comprehensive documentation site (commands, import/export, command mapping)
- **`/pex backend export`** — YAML snapshot of the active backend (modern framework)
- **`/pex backend list`**, **`switch`**, **`import`** — explicit backend admin
- [Import & Export](/guides/import-export) and [Command mapping](/commands/command-mapping) guides
- **`dev.rono.permissions.api`** as the primary integration surface for new plugins
- Lazy legacy hook API activation (only when hook plugins need it)

---

## Breaking changes

PermissionsExPlus 3.0 intentionally minimizes breaks. The items below are administrative, not data-format changes:

| Change | Impact | What to do |
|--------|--------|------------|
| Jar / project name | `PermissionsEx-1.23.4.jar` → `PermissionsExPlus-3.0.0-SNAPSHOT.jar` | Remove old jars; keep one PEX jar |
| SNAPSHOT builds | Pre-release identifier in version string | Use for testing; pin to `3.0.0` stable when released |
| `backend: file` in config | Normalized to `h2` at load time | Set `backend: h2` explicitly when convenient |
| Framework-specific admin commands | `config`, `convert uuid` = classic; `export` = modern | See [Command mapping](/commands/command-mapping) |
| `@Deprecated(since = "3.0.0")` on select legacy methods | Compile-time warnings only | Adopt [Modern API](/developers/api/modern) for new code |
| Java 21+ required | PermissionsEx 1.23.4 ran on older Java | Upgrade server JVM before installing PEX+ |

**No permission data format break** between **PermissionsEx 1.23.4** and **PermissionsExPlus 3.0.0-SNAPSHOT**. Your H2 database, SQL data, and YAML imports remain valid.

---

## Compatibility matrix

### Server platforms

| Platform | PermissionsEx 1.23.4 | PermissionsExPlus 3.0.0-SNAPSHOT |
|----------|------------------------|----------------------------------|
| Bukkit / Spigot / Paper | **Yes** (only supported platforms) | Yes |
| BungeeCord / Waterfall | **—** (no proxy module in upstream) | Yes |
| Velocity | **—** | Yes |
| Sponge | **—** | Yes |
| Minecraft versions | **1.8.x era** (upstream `1.8.8` API baseline) | **1.8.8 – 1.26.1** |

Details: [Platform Compatibility](/developers/compatibility)

### Hook plugins

| Plugin type | PermissionsEx 1.23.4 | PermissionsExPlus 3.0.0-SNAPSHOT |
|-------------|------------------------|----------------------------------|
| Legacy `ru.tehkode.*` | Works | Works — [Legacy API](/developers/api/legacy) |
| Modern `dev.rono.*` | — | Works — [Modern API](/developers/api/modern) |
| Vault consumers | Works | Works |

### Command frameworks

| Framework | PermissionsEx 1.23.4 | PermissionsExPlus 3.0.0-SNAPSHOT |
|-----------|----------------------|----------------------------------|
| Classic | Yes (only) | Yes — `config.yml` |
| `modern` (default) | — | Yes |

---

## Upgrade steps

### 1. Back up

```text
plugins/PermissionsEx/
├── config.yml
├── permissions.yml        # typical on 1.23.4
└── permissions.mv.db        # if you already migrated to H2 locally
```

For SQL backends, dump the database too.

### 2. Replace the jar

1. Stop the server
2. Remove **all** old PEX jars (`PermissionsEx.jar`, `PermissionsEx-1.23.*.jar`, module-specific jars)
3. Install **`PermissionsExPlus-3.0.0-SNAPSHOT.jar`** (build from source or CI artifact until a GitHub Release is published)
4. Ensure the server runs **Java 21+**
5. Start the server

### 3. Verify

```text
/pex version
/pex backend
/pex groups list
/pex hierarchy
```

`/pex version` should report **3.0.0-SNAPSHOT**. `/pex backend` should show your expected backend (`h2` after YAML import, or your SQL backend).

### 4. Review config (optional)

```yaml
permissions:
  command-framework: modern
  backend: h2
  backends:
    h2:
      type: h2
      database: permissions
      migration-source: permissions.yml
```

See [Configuration](/configuration) and [Example files](/guides/example-configs).

### 5. Post-upgrade checks

| Check | Command |
|-------|---------|
| Groups intact | `/pex groups list` |
| Users intact | `/pex users list` |
| Permissions resolve | `/pex user <name> check essentials.home` |
| Prefixes / weight | `/pex user <name> groups list` |
| UUID keys (if needed) | `/pex convert uuid` (classic framework) |

---

## Common scenarios

### I used YAML (`permissions.yml` only)

Leave your data in `plugins/PermissionsEx/permissions.yml`. On first startup with default `backend: h2`, PEX+ imports it into H2 and renames the file to **`permissions.yml.migrated`**.

### I already use H2 (`permissions.mv.db`)

No data migration. Drop in the PermissionsExPlus jar and start.

### My staff know classic `/pex` syntax

Keep `command-framework: classic` in `config.yml`. PermissionsExPlus features like `/pex backend export` require `modern` — switch temporarily or see [Import & Export](/guides/import-export).

### I maintain a hook plugin from PermissionsEx 1.23.4

Typical legacy plugins work without recompile. New development should use [Modern API](/developers/api/modern):

```xml
<version>3.0.0-SNAPSHOT</version>
```

### I run a proxy network

PermissionsEx **1.23.4** had **no BungeeCord, Waterfall, or Velocity module** — PEX ran on game servers only. PermissionsExPlus **3.0** adds proxy support from one jar.

Upgrade **every** backend (and your proxy, if you use one) to PermissionsExPlus 3.0. Use a shared **`sql`** backend. See [Network proxy setup](/guides/recipes/#network-proxy-bungee--backend).

---

## Rollback

1. Stop the server
2. Restore backed-up `plugins/PermissionsEx/`
3. Reinstall **`PermissionsEx-1.23.4.jar`** from the [original PermissionsEx project](https://github.com/PEXPlugins/PermissionsEx)
4. Start the server

> Run only one PEX version per server process.

---

## Related

- [Import & Export](/guides/import-export)
- [Command mapping](/commands/command-mapping)
- [Migrate from other plugins](/faq/migration)
- [Troubleshooting](/guides/troubleshooting)
- [Changelog](/changelog)
