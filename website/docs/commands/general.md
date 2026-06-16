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
| Import backend | `/pex backend import yaml-import` | `/pex import yaml-import` |

Command reference pages document **both** frameworks. **Modern** syntax is the default for new servers; classic examples are shown in a second column where helpful. See [Configuration — command framework](/configuration/#command-framework).

---

## `/pex`

**Syntax:** `/pex [help] [page] [count]`

Shows the command help menu. Without arguments, displays the first page.

```text
/pex
/pex help
/pex help 2
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

Generates a diagnostic report (PEX version, backend, config snapshot) for bug reports.

```text
/pex report
```

Paste the output when opening a [GitHub issue](https://github.com/%%site.repo%%/issues).

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

**Syntax:** `/pex backend [alias]`

Show or switch the active storage backend.

| Backend | Description |
|---------|-------------|
| `local` | H2 file database (default) — `permissions.mv.db` under `basedir` |
| `sql` | MySQL / PostgreSQL / SQLite (shared networks) |
| `memory` | In-memory (testing) |
| `yaml-import` / `file` | Legacy YAML import only — **deprecated** |

```text
/pex backend
/pex backend local
/pex backend sql
```

Switching backends does not migrate data automatically. Use `/pex import` (classic) or `/pex backend import` (modern) after switching.

---

## `/pex import`

**Syntax (classic):** `/pex import <backend>`

**Syntax (modern):** `/pex backend import <backend>`

Import permission data from another configured backend into the current one.

```text
/pex backend sql
/pex import yaml-import
```

Ensure the source backend is configured in `config.yml` under `permissions.backends`.

---

## `/pex convert uuid`

**Syntax:** `/pex convert uuid`

Bulk-converts user records from username keys to UUID keys. Run after migrating from older PEX versions.

```text
/pex convert uuid
```

Players should have joined at least once so their UUIDs are known.

---

## `/pex hierarchy`

**Syntax:** `/pex hierarchy [world]`

Prints the full user/group permission tree. Useful for auditing setups.

```text
/pex hierarchy
/pex hierarchy world_nether
```

---

## `/pex toggle debug`

**Syntax:** `/pex toggle debug`

Enables verbose permission-resolution logging in the server console.

```text
/pex toggle debug
```

Disable the same way. Use when diagnosing [permission issues](/guides/troubleshooting/).

---

## Permission node

| Node | Effect |
|------|--------|
| `permissionsex.disabled` | Disables regex permission matching for that player |

```text
/pex user Griefer add permissionsex.disabled
```

---

## Standalone rank commands

These work without the `/pex` prefix. See [Rank commands](/commands/ranks/).

```text
/promote <user> [ladder]
/demote <user> [ladder]
```
