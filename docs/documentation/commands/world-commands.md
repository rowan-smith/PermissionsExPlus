---
sidebar_position: 6
---

# World Commands

PermissionsExPlus handles world-specific permissions through context flags rather than dedicated world commands.

## Using World Context

World-scoped permissions are applied using the `--world` flag with user and group commands:

### User commands with world context

```bash
# Add a permission in a specific world
/pex user <user> permission add <permission> --world <world>

# Remove a permission in a specific world
/pex user <user> permission remove <permission> --world <world>

# List permissions in a specific world
/pex user <user> permissions list --world <world>

# Add user to a group in a specific world
/pex user <user> groups add <group> --world <world>
```

### Group commands with world context

```bash
# Add a permission in a specific world
/pex group <group> permission add <permission> --world <world>

# Set group prefix in a specific world
/pex group <group> options set prefix <prefix> --world <world>

# Add parent group in a specific world
/pex group <group> parents add <parent> --world <world>
```

## World Inheritance

As in PermissionsEx, a world can inherit from other worlds: with `world_nether`
inheriting from `world`, everything set with `--world world` (permissions, options,
prefixes and group memberships) also applies to players in `world_nether`.
Inheritance is transitive and applies to every permission check.

- `/pex worlds`: List every world's inheritance.
- `/pex world <world>`: Show the worlds `<world>` inherits from.
- `/pex world <world> inherit <worlds...>`: Set the worlds `<world>` inherits from, replacing the previous list. Separate names with spaces or commas.
- `/pex world <world> clear`: Remove `<world>`'s inheritance.

Inheritance is stored per server in `worlds.yml`, in the same shape as the `worlds`
section of a PermissionsEx `permissions.yml`:

```yaml
worlds:
  world_nether:
    inheritance:
    - world
```

When a world and a world it inherits from set the same node differently, the
`inheritance.conflict-resolution` setting in `advanced.yml` decides, as for any other
conflict between equally specific nodes. (PermissionsEx preferred the player's own
world.)
