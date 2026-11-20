---
sidebar_position: 6
---

# Default Groups

Default groups are automatically assigned to new players when they first join the server.

## Configuration

Default groups are set in the permissions data (not `config.yml`), per world:

### Via command

```bash
/pex set default group <group> true [world]
```

For example, to make the `default` group the default for all worlds:

```bash
/pex set default group default true
```

To unset a default group:

```bash
/pex set default group default false
```

### Via permissions.yml

In your `permissions.yml`, default groups are specified under each world section:

```yaml
groups:
  default:
    options:
      default: true
    permissions:
      - essentials.help
      - essentials.list
```

### Viewing default groups

```bash
/pex default group [world]
```

## Multiple Default Groups

:::note
You can have multiple default groups. A new player will be added to all groups marked as default. This is useful for giving all new players a base set of permissions while also assigning them to a specific group hierarchy.
:::
