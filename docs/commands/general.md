---
title: General Commands
description: General /pex commands for reload, config, backends, and debugging.
slug: /commands/general
---

Commands for server-wide PEX management. Requires `permissions.*` or `*`.

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

**When to use:** After editing `permissions.yml` or `config.yml` by hand.

```text
/pex reload
```

> Changes made with `/pex` commands in-game are saved immediately. Reload is mainly for manual file edits.

---

## `/pex report`

**Syntax:** `/pex report`

Generates a diagnostic report (PEX version, backend, config snapshot) for bug reports.

```text
/pex report
```

Paste the output when opening a [GitHub issue](https://github.com/.repo@/issues).

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
/pex reload
```

Common nodes: `permissions.debug`, `permissions.backend`, `permissions.allowOps`, `permissions.createUserRecords`.

---

## `/pex backend`

**Syntax:** `/pex backend [alias]`

Show or switch the active storage backend.

| Backend | Description |
|---------|-------------|
| `file` | YAML files (default) |
| `sql` | MySQL/MariaDB |
| `memory` | In-memory (testing) |

```text
/pex backend
/pex backend file
/pex backend sql
```

Switching backends does not migrate data automatically. Use `/pex import` after switching.

---

## `/pex import`

**Syntax:** `/pex import <backend>`

Import permission data from another configured backend into the current one.

```text
/pex backend sql
/pex import file
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
