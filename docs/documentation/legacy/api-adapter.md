# API Adapter

The `PermissionsExApiAdapter` provides binary compatibility with the legacy PermissionsEx 1.x API.

## Purpose

Plugins written for PermissionsEx 1.23.4 can continue to function on a server running PermissionsExPlus by using this adapter.

## Implementation Details

- It wraps the modern `PexApi` and translates calls back and forth.
- Supports `PermissionUser`, `PermissionGroup`, and `PermissionManager` legacy interfaces.
- Thin binary-compat shim: `has`/options/prefix/suffix/promote/demote resolve through Plus APIs.
- Default backend is `data`; legacy aliases redirect to the same bridge. No independent stores.
- Configuration is owned entirely by PermissionsExPlus. The adapter does not read or write `config.yml`.
- `PermissionsExConfig` is retained as a thin public facade for binary compatibility.
- Some niche v1 events may not be fully reproduced.

## PermissionsEx 1.23.6 features

The adapter tracks PermissionsEx 1.23.6:

- **Timed permissions persist.** `addTimedPermission(permission, world, seconds)` stores the
  permission with its expiry, so it survives restarts and is removed when it expires. Timed
  permissions are listed by `getTimedPermissions`, not `getOwnPermissions`, and
  `setPermissions` keeps them. A lifetime of 0 still means "until the server stops".
- **Timed group memberships** (`addGroup(group, world, seconds)`) are stored the same way.
- **Ranks and ladders.** Setting a group's rank or rank ladder places it on the matching
  PermissionsExPlus ladder, so `promote` and `demote` follow PermissionsEx ranks. A promoter
  ranked on the ladder must outrank the user they promote or demote.
- **PlaceholderAPI.** With PlaceholderAPI installed, the adapter registers the PermissionsEx
  `%pex_...%` placeholders, such as `%pex_user_prefix%` and `%pex_group_<group>_parents%`.
  See [the PermissionsEx placeholder list](/docs/integrations/placeholder-api/overview).
  PermissionsExPlus's own placeholders, `%permissionsexplus_...%`, are separate.

## Differences from PermissionsEx

- Group names are stored in lower case, so `getIdentifier()` returns `vip` for a group created
  as `VIP`.
- A user with no groups is in the default group (`default-group` in `config.yml`), as in
  PermissionsEx when a default group exists.
- Setting an option to an empty value removes it, as `setPrefix("")` did.
