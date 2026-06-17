---
title: General Commands
description: General /pex commands for reload, config, backends, and debugging.
slug: /commands/general
---

Commands for server-wide PEX management. Requires `permissions.*` or `*`.

---

## Command framework

PEX registers one of two command trees at startup, controlled by `permissions.command-framework` in `config.yml`:

| Setting | Framework | Example |
|---------|-----------|---------|
| `modern` (default) | Cloud-based structured subcommands | `/pex user Steve permissions add essentials.home` |
| `classic`, `legacy`, or `old` | Original PEX syntax | `/pex user Steve add essentials.home` |

Check which tree is active:

```text
/pex config permissions.command-framework
```

Switch in `config.yml` and reload (or restart):

```yaml
permissions:
  command-framework: classic   # or modern
```

### Modern vs classic examples

| Task | Modern | Classic |
|------|--------|---------|
| Add permission | `/pex user Steve permissions add essentials.home` | `/pex user Steve add essentials.home` |
| List permissions | `/pex user Steve permissions list` | `/pex user Steve list` |
| Add to group | `/pex user Steve groups add vip` | `/pex user Steve group add vip` |
| Switch backend | `/pex backend switch sql` | `/pex backend sql` |
| Import backend | `/pex backend import file` | `/pex import file` |
| Export backend | `/pex backend export` | — |
| Toggle debug | `/pex debug on` / `/pex debug off` | `/pex toggle debug` |
| Edit config in-game | — | `/pex config permissions.debug true` |

Command reference pages document **modern** syntax first (the default). Classic equivalents appear in [Command mapping](/commands/command-mapping) and on [Rank commands](/commands/ranks#classic-promote-and-demote) where they differ.

See also [Configuration — command framework](/configuration/#command-framework) and [Import & Export](/guides/import-export).

---

## `/pex`

**Syntax:** `/pex` · `/pex help`

Shows the command summary for the active framework. Paginated help (`/pex help 2`) is not supported in the modern framework.

```text
/pex
/pex help
```

---

## `/pex reload`

**Syntax:** `/pex reload`

Reloads `config.yml`, the active backend, and all in-memory permission data.

**When to use:** After editing `config.yml` by hand.

```text
/pex reload
```

> Changes made with `/pex` commands in-game are saved immediately. Reload is mainly for manual config edits.

---

## `/pex report`

**Syntax:** `/pex report`

Shows where to report issues (typically a link to the GitHub issue tracker). For version and backend details, use `/pex version` and `/pex backend`.

```text
/pex report
/pex version
/pex backend
```

Include `/pex version` output when opening a [GitHub issue](https://github.com/%%site.repo%%/issues).

---

## `/pex config`

**Syntax:** `/pex config <node> [value]`

View or change a config node from `config.yml`.

| Argument | Description |
|----------|-------------|
| `node` | Dot-separated path, e.g. `permissions.debug` |
| `value` | New value (omit to read current) |

```text
/pex config permissions.debug
/pex config permissions.debug true
/pex config permissions.backend
/pex config permissions.command-framework
/pex reload
```

Common nodes: `permissions.debug`, `permissions.backend`, `permissions.command-framework`, `permissions.allowOps`, `permissions.createUserRecords`.

---

## `/pex backend`

Show, switch, list, import, and export storage backends.

| Backend | Description |
|---------|-------------|
| `local` | H2 file database (default) — `permissions.mv.db` under `basedir` |
| `sql` | MySQL / PostgreSQL / SQLite (shared networks) |
| `memory` | In-memory (testing) |
| `file` | YAML import only (not for active storage) |

### Show active backend

| Framework | Syntax |
|-----------|--------|
| Both | `/pex backend` |
| Modern | `/pex backend info` |

```text
/pex backend
```

### List configured backends (modern)

**Syntax:** `/pex backend list`

```text
/pex backend list
```

### Switch backend

| Framework | Syntax |
|-----------|--------|
| Modern | `/pex backend switch <alias>` |
| Classic | `/pex backend <alias>` |

```text
/pex backend switch local
/pex backend sql
```

Switching backends does **not** migrate data automatically. Import after switching — see [Import & Export](/guides/import-export).

---

## `/pex import`

**Syntax (classic):** `/pex import <backend>`

**Syntax (modern):** `/pex backend import <backend>`

Import permission data from another configured backend into the current one. Data is **merged** into the active backend.

```text
/pex backend switch local
/pex backend import yaml-import
```

Ensure the source backend is configured in `config.yml` under `permissions.backends`. Full walkthrough: [Import & Export](/guides/import-export).

---

## `/pex export`

**Syntax (modern):** `/pex backend export [backend]`

Dumps the active (or named) backend as YAML text in chat. Use for backups and preparing import files.

```text
/pex backend export
/pex backend export local
```

> Export is available in the **modern** command framework only. Classic servers can switch to `command-framework: modern` temporarily, or back up `permissions.mv.db` / use SQL dump tools.

---

## `/pex version`

**Syntax:** `/pex version`

Shows the installed PermissionsExPlus version.

```text
/pex version
```

---

## `/pex convert uuid`

**Syntax (classic):** `/pex convert uuid` · `/pex convert uuid force`

Bulk-converts user records from username keys to UUID keys. Run after migrating from older PEX versions.

```text
/pex convert uuid
/pex convert uuid force
```

> UUID conversion is registered in the **classic** command framework. Use `command-framework: classic` in `config.yml` if you need this command in-game.

Players should have joined at least once so their UUIDs are known. `force` rewrites records even when a UUID mapping already exists — use with care.

---

## `/pex hierarchy`

**Syntax:** `/pex hierarchy [world]`

Prints the full user/group permission tree. Useful for auditing setups.

```text
/pex hierarchy
/pex hierarchy world_nether
```

---

## Debug logging

| Framework | Turn on | Turn off | Status |
|-----------|---------|----------|--------|
| **Modern** | `/pex debug on` | `/pex debug off` | `/pex debug` |
| **Classic** | `/pex toggle debug` | `/pex toggle debug` (toggles) | — |

Enables verbose permission-resolution logging in the server console. Use when diagnosing [permission issues](/guides/troubleshooting/).

```text
/pex debug on
/pex toggle debug
```

---

## Permission node

| Node | Effect |
|------|--------|
| `permissionsex.disabled` | Disables regex permission matching for that player |

```text
/pex user Griefer add permissionsex.disabled
```

---

## Standalone rank commands (classic only)

When `command-framework` is **classic**, these work without the `/pex` prefix. The **modern** framework uses [`/pex ladder`](/commands/ranks) instead.

```text
/promote <user> [ladder]
/demote <user> [ladder]
```
