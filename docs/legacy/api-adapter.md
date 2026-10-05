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
