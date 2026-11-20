# PlaceholderAPI

PermissionsExPlus integrates with PlaceholderAPI to provide dynamic information about players and their permissions.

## Placeholders

Use these placeholders in any plugin that supports PlaceholderAPI.

| Placeholder | Description | Example Output |
| :--- | :--- | :--- |
| `%pexplus_group%` | The player's primary group name. | `admin` |
| `%pexplus_groups%` | Comma-separated list of all player groups. | `admin, vip` |
| `%pexplus_prefix%` | The player's current prefix. | `[Admin] ` |
| `%pexplus_suffix%` | The player's current suffix. | ` &6[VIP]` |
| `%pexplus_has_permission_<permission>%` | Returns `yes` or `no` if the player has a permission. | `yes` |
| `%pexplus_option_<key>%` | Returns the value of a specific option. | `100` (for weight) |

## Resolution Examples

### Using in Chat

If you use a chat plugin like EssentialsX Chat or DeluxeChat, you can use `%pexplus_prefix%` to show the player's rank prefix.

### Using in Tab List

In TabList plugins, you can use `%pexplus_group%` to sort players or display their group name.

### Conditional Placeholders

You can check if a player has a specific permission to show custom icons:
`%javascript_is_staff%` (custom script) could use `%pexplus_has_permission_staff.mode%`.
