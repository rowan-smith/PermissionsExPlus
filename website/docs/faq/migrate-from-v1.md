---
title: Migrating from Version 1
description: Upgrade from PermissionsExPlus 1.24.x to 3.0.0-SNAPSHOT — a new major line with full backwards compatibility.
slug: /faq/migrate-from-v1
---

PermissionsExPlus **3.0.0-SNAPSHOT** begins a **new major version line**. It is a deliberate fresh start for the project — new defaults, documentation, and API investment — while keeping **full backwards compatibility** with Version 1 servers, data, commands, and hook plugins.

If you are on **1.24.x** (the last Version 1 release), you can upgrade in place. No permission data rewrite is required.

> **Not on PermissionsExPlus yet?** See [Migrate from other plugins](/faq/migration) for original PEX, LuckPerms, GroupManager, and similar.

---

## Version numbering

| Line | Maven / jar versions | Status |
|------|----------------------|--------|
| **Version 1** | `1.23.x` – **`1.24.x`** (latest: `1.24.0`) | Maintenance ended — upgrade to Version 3 |
| **Version 3** | **`3.0.0-SNAPSHOT`** → `3.0.0` (stable) | Current — new start |

The jump **`1.24.x` → `3.0.0-SNAPSHOT`** is intentional. Version 3 resets the major number to signal a new chapter for PermissionsExPlus — not a missing `1.25` or `2.0` release. Earlier `1.23.x` builds are part of the same Version 1 line and follow the same upgrade path.

---

## Backwards compatibility promise

Version 3 is a **new start**, not a breaking rewrite. The following carry over from 1.24.x without conversion:

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

What changes is the **version label**, **documentation focus**, and **new Version 3 features** — not your existing permission data.

---

## What is new in Version 3

### Philosophy

- **New major line** — `3.0.0-SNAPSHOT` is the development baseline; `3.0.0` will be the first stable Version 3 release
- **Backwards compatible by default** — 1.24.x servers upgrade without data migration
- **Modern-first** — structured commands, export/import workflows, and `dev.rono.permissions.api` are the documented path forward
- **Legacy preserved** — classic commands and `ru.tehkode.*` hooks remain for existing ecosystems

### Defaults (unchanged from late 1.x, now documented as Version 3 baseline)

| Area | Version 1 (`1.24.x`) | Version 3 (`3.0.0-SNAPSHOT`) |
|------|----------------------|------------------------------|
| Storage backend | H2 `local` | **`local` (H2)** |
| Command framework | `modern` + `classic` | **`modern`** default |
| YAML day-to-day storage | Import/migration only | **Import/migration only** |
| Java runtime | Java 21+ | **Java 21+** |

### New in Version 3

- Comprehensive documentation site (commands, import/export, command mapping)
- **`/pex backend export`** — YAML snapshot of the active backend (modern framework)
- **`/pex backend list`**, **`switch`**, **`import`** — explicit backend admin
- [Import & Export](/guides/import-export) and [Command mapping](/commands/command-mapping) guides
- **`dev.rono.permissions.api`** as the primary integration surface for new plugins

---

## Breaking changes

Version 3 intentionally minimizes breaks. The items below are administrative, not data-format changes:

| Change | Impact | What to do |
|--------|--------|------------|
| Jar version in filename | `PermissionsExPlus-1.24.x.jar` → `PermissionsExPlus-3.0.0-SNAPSHOT.jar` | Remove old jars; keep one PEX jar |
| SNAPSHOT builds | Pre-release identifier in version string | Use for testing; pin to `3.0.0` stable when released |
| `backend: file` in config | Normalized to `local` at load time | Set `backend: local` explicitly when convenient |
| Framework-specific admin commands | `config`, `convert uuid` = classic; `export` = modern | See [Command mapping](/commands/command-mapping) |
| `@Deprecated(since = "1.24.x")` on select legacy methods | Compile-time warnings only | Adopt [Modern API](/developers/api/modern) for new code |

**No permission data format break** between `1.24.x` and `3.0.0-SNAPSHOT`. Your H2 database, SQL data, and YAML imports remain valid.

---

## Compatibility matrix

### Server platforms

| Platform | 1.24.x | 3.0.0-SNAPSHOT |
|----------|--------|----------------|
| Spigot / Paper | Yes | Yes |
| BungeeCord / Waterfall | Yes | Yes |
| Velocity | Yes | Yes |
| Sponge | Yes | Yes |
| Minecraft `1.8.8` – `1.26.1` | Yes | Yes |

Details: [Platform Compatibility](/developers/compatibility)

### Hook plugins

| Plugin type | 1.24.x | 3.0.0-SNAPSHOT |
|-------------|--------|----------------|
| Legacy `ru.tehkode.*` | Works | Works — [Legacy API](/developers/api/legacy) |
| Modern `dev.rono.*` | Works | Works — [Modern API](/developers/api/modern) |
| Vault consumers | Works | Works |

### Command frameworks

| Framework | 1.24.x | 3.0.0-SNAPSHOT |
|-----------|--------|----------------|
| `modern` (default) | Yes | Yes |
| `classic` / `legacy` / `old` | Yes | Yes — `config.yml` |

---

## Upgrade steps

### 1. Back up

```text
plugins/PermissionsEx/
├── config.yml
├── permissions.mv.db
└── permissions.yml        # only if not yet migrated
```

For SQL backends, dump the database too.

### 2. Replace the jar

1. Stop the server
2. Remove **all** old PEX jars (`PermissionsEx.jar`, `PermissionsExPlus-1.24.*.jar`, `PermissionsExPlus-1.23.*.jar`, module-specific jars)
3. Install **`PermissionsExPlus-3.0.0-SNAPSHOT.jar`** (build from source or CI artifact until a GitHub Release is published)
4. Start the server

### 3. Verify

```text
/pex version
/pex backend
/pex groups list
/pex hierarchy
```

`/pex version` should report **3.0.0-SNAPSHOT**. `/pex backend` should show your expected backend.

### 4. Review config (optional)

```yaml
permissions:
  command-framework: modern
  backend: local
  backends:
    local:
      type: local
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
| Prefixes / weight | `/pex user <name> group list` |
| UUID keys (if needed) | `/pex convert uuid` (classic framework) |

---

## Common scenarios

### Upgrading from 1.23.x (earlier Version 1)

Same process as 1.24.x. The Version 1 line includes all `1.23.x` and `1.24.x` builds. Back up, replace the jar, verify with `/pex hierarchy`.

### I used YAML (`backend: file`)

Configs with `backend: file` are normalized to `local` automatically. YAML path is preserved as `migration-source`.

### I already use H2 (`permissions.mv.db`)

No data migration. Drop in the Version 3 jar and start.

### My staff know classic `/pex` syntax

Keep `command-framework: classic` in `config.yml`. Version 3 features like `/pex backend export` require `modern` — switch temporarily or see [Import & Export](/guides/import-export).

### I maintain a hook plugin from 1.24.x

Typical legacy plugins work without recompile. New development should use [Modern API](/developers/api/modern):

```xml
<version>3.0.0-SNAPSHOT</version>
```

### I run a proxy network

Upgrade **every** backend and proxy to Version 3. Use a shared **`sql`** backend. See [Network proxy setup](/guides/recipes/#network-proxy-bungee--backend).

---

## Rollback

1. Stop the server
2. Restore backed-up `plugins/PermissionsEx/`
3. Reinstall your last **1.24.x** jar (e.g. `PermissionsExPlus-1.24.0.jar`)
4. Start the server

> Run only one PEX version per server process.

---

## Related

- [Import & Export](/guides/import-export)
- [Command mapping](/commands/command-mapping)
- [Migrate from other plugins](/faq/migration)
- [Troubleshooting](/guides/troubleshooting)
- [Changelog](/changelog)
