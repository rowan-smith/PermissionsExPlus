---
title: Migrate from Other Plugins
description: Migrating to PermissionsExPlus from other permission plugins.
slug: /faq/migration
---

Upgrading from **PermissionsEx 1.23.4**? See [Migrating from PermissionsEx 1.23.4](/faq/migrate-from-v1) for the **`1.23.4` → `3.0.0-SNAPSHOT`** upgrade with full backwards compatibility.

---

## From original PermissionsEx

1. **Back up** your `plugins/PermissionsEx/` folder
2. Remove old PEX jars from `plugins/`
3. Install **`PermissionsExPlus-%%site.version%%.jar`**
4. Start the server — if `permissions.yml` is present, the default **`h2`** backend imports it into H2 and renames the file to **`permissions.yml.migrated`**
5. Run `/pex` to verify; check `/pex backend` shows `h2`

Your groups, users, and permissions are preserved in the H2 database. Command syntax depends on `permissions.command-framework` — see [Command framework](/commands/general/#command-framework).

If you prefer the old command tree, set `command-framework: classic` in `config.yml`.

## From YAML-only setups

PermissionsExPlus no longer uses YAML as the default store. On first startup:

1. Leave (or place) your data in `plugins/PermissionsEx/permissions.yml`
2. Use the default `backend: h2` — PEX creates `permissions.mv.db` and migrates the YAML
3. The original file becomes `permissions.yml.migrated`

To import YAML later without switching backends permanently, configure a `file` backend section and run `/pex import file` (classic) or `/pex backend import file` (modern).

## From GroupManager / bPermissions

PEX uses a different file format. You will need to recreate groups:

```text
/pex group admin create
/pex group admin add '*'
/pex user <name> group set admin
```

Or import via a backend if you have SQL data set up.

## From LuckPerms

LuckPerms and PEX are separate systems. To switch:

1. Export your LuckPerms data (or note your group structure)
2. Install PermissionsExPlus
3. Recreate groups with `/pex group` commands or prepare a `permissions.yml` for one-time H2 migration
4. Remove LuckPerms

There is no automatic LuckPerms → PEX converter yet.

## From file backend configs

Configs with `backend: file` are **automatically normalized to `h2`** at load time. The YAML path from `permissions.backends.file.file` is copied to `permissions.backends.h2.migration-source` so existing `permissions.yml` files still import on first startup. You can update `config.yml` to `backend: h2` explicitly when convenient.

## UUID migration

If your old data uses player names instead of UUIDs:

```text
/pex convert uuid
```

Run this after your regular players have joined at least once so PEX can resolve their UUIDs.

## Tips

- Always **back up** before migrating
- Test on a staging server first
- Use `/pex hierarchy` to verify the result
- Use `/pex import <backend>` (classic) or `/pex backend import <backend>` (modern) when moving between storage backends
