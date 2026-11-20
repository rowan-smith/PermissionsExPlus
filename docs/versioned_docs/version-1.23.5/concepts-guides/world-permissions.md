---
sidebar_position: 7
---

# World Permissions

Permissions can be scoped to a specific world. World-specific permissions are applied in addition to global permissions.

:::note
World-specific permissions are additive, they don't replace your global permissions, they add to them.
:::

## Configuration

```yaml
groups:
  default:
    permissions:
      - essentials.help          # Global: works in all worlds
    worlds:
      creative:
        permissions:
          - essentials.fly       # Only granted in the creative world
```

## Managing world permissions

```bash
/pex user <user> add <permission> creative
/pex group <group> add <permission> survival
```

For world-specific inheritance, see [Inheritance](inheritance#world-specific-inheritance).
