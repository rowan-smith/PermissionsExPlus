---
title: Migrate from Other Plugins
description: Migrating to PermissionsExPlus from other permission plugins.
slug: /faq/migration
---

## From original PermissionsEx

1. **Back up** your `plugins/PermissionsEx/` folder
2. Remove old PEX jars from `plugins/`
3. Install **`PermissionsExPlus-%%site.version%%.jar`**
4. Start the server — existing `permissions.yml` should load as-is
5. Run `/pex` to verify

Your groups, users, and permissions are preserved. Commands work the same way.

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
3. Recreate groups with `/pex group` commands or write `permissions.yml`
4. Remove LuckPerms

There is no automatic LuckPerms → PEX converter yet.

## From YAML / manual setup

Copy the [example configuration](/configuration/) and adjust group names and permissions to match your server.

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
- Use `/pex import <backend>` if moving between storage backends
