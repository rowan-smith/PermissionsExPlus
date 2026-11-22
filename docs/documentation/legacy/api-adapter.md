# API Adapter

The `PermissionsExApiAdapter` provides binary compatibility with the legacy PermissionsEx 1.x API.

## Purpose

Plugins written for PermissionsEx 1.23.4 can continue to function on a server running PermissionsExPlus by using this adapter.

## Versioning

The adapters are versioned separately from PermissionsExPlus. Their version is the
PermissionsEx 1.x release whose API and commands they reproduce, currently 1.23.6, so
`PermissionsExApiAdapter-1.23.6` runs plugins written for PermissionsEx up to 1.23.6 on
PermissionsExPlus 2.x.

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

## Compatibility with existing plugins

The adapter keeps every public member of the PermissionsEx 1.23.4 and 1.23.5 APIs, including
the static `PermissionsEx.getUser(Player)` and `PermissionsEx.getUser(String)` that 1.23.5 removed
but most older plugins call, and `PermissionUser.setGroups(List)` from 1.21. It is tested with:

- **Vault 1.7.3.** Vault's own PermissionsEx hooks, which Vault registers ahead of other providers,
  for permissions, groups, prefixes and options.
- **Plugins of the 1.23.4 era** (ColorMe, FactionsChat, PermissionsExAddon, PexManager,
  PrefixedPlayerlist, WordRankUp and ChatEx): every PermissionsEx call they make links against the
  adapter, and they run on Paper 1.8.8, 1.12.2 and 1.21.4 with a player joining and chatting.

To check a plugin of your own, put its jar in a folder and run:

```bash
mvn -pl PermissionsExApiAdapter test -Dtest=LegacyPluginLinkageTest -Dpex.legacyPlugins=/path/to/folder
```

It lists any PermissionsEx method, constructor or field the plugin uses that the adapter lacks.

Vault 1.7.3's group *info* setters (`setGroupInfoString` and the like) pass their arguments in the
wrong order and never worked with any PermissionsEx; reading group info through Vault works.

## Differences from PermissionsEx

- Group names are stored in lower case, so `getIdentifier()` returns `vip` for a group created
  as `VIP`.
- A user with no groups is in the default group (`default-group` in `config.yml`), as in
  PermissionsEx when a default group exists.
- Setting an option to an empty value removes it, as `setPrefix("")` did.
