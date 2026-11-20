---
sidebar_position: 3
---

# Permissions File

PermissionsExPlus stores user and group data in `permissions.yml` (when using the file backend) or in the configured database. This page explains the data structure and how to manage it.

## File Location

```text
plugins/PermissionsEx/permissions.yml
```

## Structure

```yaml
users:
  <user-identifier>:                       # UUID is preferred over username
    group:
    - member                               # Group the user is apart of
    permissions:
      - <permission.node>                  # Grant a permission
      - -essentials.troll                  # Negate a permission (deny it)
      - essentials.troll:<unix_timestamp>  # Grant a timed permission
    options:
      name: <last-known-name>              # Last known Minecraft username
      prefix: <prefix>                     # Chat prefix (e.g. "&c" for red)
      suffix: <suffix>                     # Chat suffix
      <custom-option>: <value>             # Custom key-value metadata
    worlds:
      <world-name>:
        permissions:
          - <world-specific.permission>    # Permission only in this world
        options:
          prefix: <world-prefix>           # Chat prefix for this world
          suffix: <world-suffix>           # Chat suffix for this world
          <custom-option>: <value>         # Option only in this world

groups:
  <group-name>:
    inheritance:
      - <parent-group>                     # Inherit permissions from this group
    permissions:
      - <permission.node>                  # Grant a permission to all members
      - -essentials.troll                  # Negate a permission
      - essentials.troll:<unix_timestamp>  # Grant a timed permission to all members
      - #essentials.fly                    # Non-inheritable permission
    options:
      default: true                        # Auto-assign to new players
      prefix: <prefix>                     # Chat prefix for all members
      suffix: <suffix>                     # Chat suffix for all members
      weight: <number>                     # Higher = higher priority in multiple groups
      rank: <number>                       # Lower number = higher position in ladder
      rank-ladder: <ladder>                # Ladder name (default if omitted)
      <custom-option>: <value>             # Custom key-value metadata
    worlds:
      <world-name>:
        permissions:
          - <world-specific.permission>    # Permission only in this world
        options:
          <custom-option>: <value>         # Option only in this world
```

## Related Pages

- [Permission Nodes](../concepts-guides/permission-nodes): syntax and format
- [Negation](../concepts-guides/negation): denying specific permissions
- [Wildcards](../concepts-guides/wildcards): matching multiple nodes with `*`
- [Timed Permissions](../concepts-guides/timed-permissions): expiring permissions with duration
- [Default Groups](../concepts-guides/default-groups): auto-assigning groups to new players
- [PlaceholderAPI](../integrations/placeholder-api/overview): placeholder expansion for chat plugins and scoreboards
