---
sidebar_position: 4
---

# Group Placeholders

This page documents PlaceholderAPI placeholders related to groups.

## Supported placeholders

| Placeholder                                       | Description                            | Example           |
|---------------------------------------------------|----------------------------------------|-------------------|
| `%pex_group_<group>_exists%`                      | Check if group exists                  | `true`            |
| `%pex_group_<group>_prefix%`                      | Group's direct `prefix` option         | `&c[Admin]`       |
| `%pex_group_<group>_suffix%`                      | Group's direct `suffix` option         | `&r`              |
| `%pex_group_<group>_parents%`                     | Parent groups, comma-separated         | `staff,moderator` |
| `%pex_group_<group>_parent_count%`                | Number of parent groups                | `2`               |
| `%pex_group_<group>_permission_count%`            | Permissions directly assigned to group | `42`              |
| `%pex_group_<group>_effective_permission_count%`  | Permissions including inherited        | `156`             |
| `%pex_group_<group>_has_permission_<permission>%` | Effective permission check             | `true`            |
| `%pex_group_<group>_option_<option>%`             | Arbitrary option value (effective)     | `c`               |

## World context

Append `_world_<world>` to check options or permissions in a specific world.
