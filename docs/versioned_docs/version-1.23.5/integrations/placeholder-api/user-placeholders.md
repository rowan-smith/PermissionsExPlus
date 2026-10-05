---
sidebar_position: 3
---

# User Placeholders

This page documents the PlaceholderAPI placeholders related to users (current player or arbitrary user queries).

## Supported placeholders

| Placeholder                              | Description                                 | Example            |
|------------------------------------------|---------------------------------------------|--------------------|
| `%pex_user_name%`                        | Player's current name                       | `Steve`            |
| `%pex_user_primary_group%`               | Primary group in the current world          | `member`           |
| `%pex_user_groups%`                      | Effective groups, comma-separated           | `member,supporter` |
| `%pex_user_direct_groups%`               | Directly assigned groups                    | `vip`              |
| `%pex_user_prefix%`                      | Effective `prefix` option                   | `&3[M]`            |
| `%pex_user_suffix%`                      | Effective `suffix` option                   | `&3`               |
| `%pex_user_option_<option>%`             | Arbitrary option value                      | `5`                |
| `%pex_user_permission_count%`            | Effective permission count                  | `42`               |
| `%pex_user_direct_permission_count%`     | Direct permission count                     | `5`                |
| `%pex_user_has_permission_<permission>%` | Permission check                            | `true`             |
| `%pex_user_in_group_<group>%`            | Group membership check (includes inherited) | `true`             |
| `%pex_user_in_group_direct_<group>%`     | Direct group membership check               | `false`            |

## Arbitrary user queries

Use `%pex_user_<username>_<placeholder>%` to query another user by name (example: `%pex_user_Alex_prefix%`).

## World context

Append `_world_<world>` to any placeholder to specify world context (last `_world_` split is used).
