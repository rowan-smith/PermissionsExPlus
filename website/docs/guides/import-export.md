---
title: Import & Export
description: Move permission data between YAML, H2, and SQL backends in PermissionsExPlus.
slug: /guides/import-export
---

PermissionsExPlus stores groups, users, and world inheritance in a **backend**. This guide covers every way to move that data in or out — first-time YAML migration, manual imports, backups, and network setup.

---

## How storage works

| Backend | Role |
|---------|------|
| **local** (default) | Day-to-day storage — embedded H2 file at `plugins/PermissionsEx/permissions.mv.db` |
| **sql** | Shared MySQL / PostgreSQL / SQLite for proxy networks |
| **memory** | Testing only — data is lost on restart |
| **file** | YAML **import source** — not for active storage |

The active backend is set by `permissions.backend` in `config.yml`. Check it in-game:

```text
/pex backend
```

Modern command framework: `/pex backend info`

See [Storage](/storage) and [Configuration](/configuration) for full backend options.

---

## Automatic YAML migration (first startup)

The easiest import path — no commands required.

1. Place your data in `plugins/PermissionsEx/permissions.yml`
2. Use the default `backend: local` in `config.yml`
3. Start the server

PEX creates `permissions.mv.db`, imports the YAML, and renames the original file to **`permissions.yml.migrated`**. Ongoing changes are stored in H2.

> This is a **one-way** migration. After import, manage data with `/pex` commands or export (see below) — do not edit `permissions.mv.db` by hand.

Sample YAML structure: [Example configuration files](/guides/example-configs) · repository [`examples/permissions.yml`](https://github.com/%%site.repo%%/blob/main/examples/permissions.yml)

---

## Manual import from YAML

Use this when you already have an H2 database and want to pull in a YAML file, or when migrating from another server.

### Step 1 — Configure a file backend alias

Add a `file` section under `permissions.backends` in `config.yml`:

```yaml
permissions:
  backend: local
  backends:
    local:
      type: local
      database: permissions
    yaml-import:
      type: file
      file: plugins/PermissionsEx/import.yml
```

Copy your YAML data to `plugins/PermissionsEx/import.yml` (or point `file` at any path).

### Step 2 — Run the import command

| Framework | Command |
|-----------|---------|
| **Modern** (default) | `/pex backend import yaml-import` |
| **Classic** | `/pex import yaml-import` |

```text
/pex backend import yaml-import
```

Import **merges** data into the active backend. Existing groups and users with the same name are updated; new entries are added.

### Step 3 — Verify

```text
/pex groups list
/pex users list
/pex hierarchy
```

---

## Import between backends

Switching the active backend does **not** copy data automatically. Use import after switching.

**Example: move from H2 to MySQL**

1. Configure the `sql` backend in `config.yml`
2. Switch to it:

   | Framework | Command |
   |-----------|---------|
   | Modern | `/pex backend switch sql` |
   | Classic | `/pex backend sql` |

3. Import from the old backend alias:

   | Framework | Command |
   |-----------|---------|
   | Modern | `/pex backend import local` |
   | Classic | `/pex import local` |

4. Verify with `/pex hierarchy`

> Always **back up** `plugins/PermissionsEx/` (or your SQL database) before switching backends.

---

## Export (backup)

Export dumps the active backend as YAML text in chat. Useful for backups, audits, and preparing import files.

| Framework | Command |
|-----------|---------|
| **Modern** | `/pex backend export` |
| **Classic** | Not available — switch to modern or copy `permissions.yml.migrated` / use SQL dump tools |

Export a specific configured backend without switching:

```text
/pex backend export local
/pex backend export yaml-import
```

Copy the YAML output from chat into a file (e.g. `backup-2026-06-17.yml`). You can re-import it later via a `file` backend alias.

---

## Common workflows

### Back up before a major change

```text
/pex backend export
```

Save the YAML output, then proceed with your changes. If something goes wrong, restore via a `file` backend and `/pex backend import`.

### Move a single-server setup to a network

1. Export on the source server: `/pex backend export`
2. Save output as `import.yml` on the new SQL-backed server
3. Configure `sql` backend and a `file` alias pointing at `import.yml`
4. `/pex backend switch sql`
5. `/pex backend import <file-alias>`

### Migrate from original PermissionsEx

1. Back up `plugins/PermissionsEx/`
2. Replace the old jar with `PermissionsExPlus-%%site.version%%.jar`
3. Start the server — existing `permissions.yml` or H2 data is preserved
4. Run `/pex convert uuid` if your data still uses player names (classic framework only)
5. Upgrading from **PermissionsEx 1.23.4**? See [Migrating from PermissionsEx 1.23.4](/faq/migrate-from-v1) — new major line, full backwards compatibility
6. See [Migration FAQ](/faq/migration) for other plugin-specific notes

### Import from GroupManager / LuckPerms

There is no automatic converter. Recreate groups with `/pex group` commands, prepare a `permissions.yml` for one-time H2 migration, or import via SQL if you have exported data in a compatible format.

---

## YAML format reference

```yaml
schema-version: 1

groups:
  default:
    default: true
    permissions:
      - modifyworld
  admin:
    inheritance:
      - default
    prefix: '&c[Admin] '
    permissions:
      - '*'

users:
  069a79f4-44e9-4726-a5be-fca90e38aaf5:
    group:
      - admin
```

| Key | Meaning |
|-----|---------|
| `inheritance` | Parent groups (see [Inheritance](/concepts/inheritance)) |
| `default: true` | Default group for new players |
| `prefix` / `suffix` | Chat meta (see [Prefix & Meta](/concepts/meta)) |
| User UUID keys | Preferred — run `/pex convert uuid` for legacy name keys |

Full option reference: [Configuration](/configuration#permission-data)

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Import says backend not found | Check the alias exists under `permissions.backends` in `config.yml`, then `/pex reload` |
| Data missing after import | Run `/pex hierarchy` — import merges; conflicting names overwrite |
| YAML not imported on first start | File must be named `permissions.yml` (or match `migration-source`) **before** first startup with an empty database |
| Wrong command syntax | Check `permissions.command-framework` — see [Command mapping](/commands/command-mapping) |
| Export output truncated in chat | Copy in sections or use `/pex report` for diagnostics; consider SQL dump for large datasets |

See also [Troubleshooting](/guides/troubleshooting) and [Storage](/storage).
