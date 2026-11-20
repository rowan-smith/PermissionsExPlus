# Migrating from PermissionsEx 1.23.4

PermissionsExPlus is designed to be a modern successor to PEX 1.x, but there are important differences.

## Key Differences

- **API**: The new API is asynchronous and uses immutable snapshots.
- **Contexts**: Arbitrary contexts replace the old world-based scoping; world data uses a `world` context.
- **Storage**: The data model is updated for better performance and flexibility.
- **Node order**: PermissionsEx used the first matching node in list order. PermissionsExPlus
  uses the most specific match, and the `inheritance.conflict-resolution` setting for ties.

## How to Migrate

1. **Back up** your PermissionsEx `permissions.yml` or SQL database.
2. **Install** PermissionsExPlus and start the server once, leaving the `PermissionsEx` folder in place
   (remove the PermissionsEx jar itself).
3. **Import**: run `/pex import permissionsex` from the console. It reads
   `plugins/PermissionsEx/config.yml` and imports from the backend it selects, either `file`
   (`permissions.yml`) or `sql` (MySQL, MariaDB, PostgreSQL or SQLite). Pass a folder to import
   from somewhere else: `/pex import permissionsex /path/to/PermissionsEx`.
4. **Read the report**. It lists what was imported, then anything that needs your attention.
5. **Compatibility Adapter**: Use `PermissionsExApiAdapter` if you have other plugins that depend on the old PEX API.

The import merges into existing PermissionsExPlus data, so it is safe to run more than once.

## What the import maps

| PermissionsEx | PermissionsExPlus |
| --- | --- |
| Group and user permissions; `-node` | Permission nodes; `-node` becomes a denied node |
| `worlds.<world>` sections | Nodes with a `world` context |
| Options, `prefix`, `suffix` | Option nodes |
| `weight` option | Group weight |
| `rank` and `rank-ladder` options | Ladders, ordered from the lowest rank |
| Group `inheritance`, user `group` | Parent groups and memberships |
| `group-<name>-until` options | Membership expiry; already-expired memberships are skipped |
| `worlds.<world>.inheritance` | [World inheritance](../commands/world-commands.md#world-inheritance) |

## Things the report may ask you to fix

- **Users stored by name.** PermissionsEx 1.23 stores users by UUID. Users still stored by
  name are skipped, because their UUIDs cannot be known offline. Let PermissionsEx convert
  them first.
- **Regular-expression permissions** such as `modifyworld.(place|break)` are kept but matched
  literally. Rewrite them as plain nodes or wildcards.
- **Default group.** PermissionsExPlus has one default group, `default-group` in
  `config.yml`. If PermissionsEx marked a different group as default, the report says which.
