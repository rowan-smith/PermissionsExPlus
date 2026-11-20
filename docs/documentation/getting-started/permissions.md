---
sidebar_position: 4
---

# Permissions

Permissions control what actions players can perform on your server. PermissionsExPlus provides a powerful and flexible system for managing these permissions.

## Negative Permissions

Sometimes you want to revoke a permission that a player would otherwise have (e.g., from a group they are in). You can do this by prefixing the permission with a hyphen `-`.

Example:
```bash
/pex user Rowan permission add -essentials.fly
```
This will prevent the user `Rowan` from flying, even if they are in a group that has the `essentials.fly` permission.

## Shorthand Permissions (Regex-like)

PermissionsExPlus supports shorthand notation to add multiple related permissions at once.

### Wildcards

You can use `*` as a wildcard to match any sub-permission.

Example:
- `essentials.*`: Matches all permissions starting with `essentials.`.
- `*`: Matches every single permission (Superuser).

### Character Sets and Ranges

You can use curly braces `{}` to specify a set of options or a range.

Example:
- `essentials.{spawn,home,warp}`: Grants `essentials.spawn`, `essentials.home`, and `essentials.warp`.
- `server.warp.{1-5}`: Grants `server.warp.1`, `server.warp.2`, `server.warp.3`, `server.warp.4`, and `server.warp.5`.

## Permission Resolution Order

When a permission check is performed, PEX+ follows this order:

1. **Directly assigned permissions**: Checked first. If a match is found (allow or deny), it returns.
2. **Inherited permissions**: Checked from highest weight group to lowest.
3. **Contextual permissions**: Permissions with specific contexts (like `--world survival`) only apply when those contexts match the current query.
4. **Default permissions**: If no match is found, the engine may check default group permissions.

## Precedence Rules

- **Exact matches** take precedence over **wildcard matches**.
- **Negative permissions** (denies) act as explicit overrides.
- **Group weight** determines which group's permissions are checked first during inheritance.
