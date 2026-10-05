---
sidebar_position: 1
---

# Permission Nodes

Permission nodes are dot-separated identifiers that control what a player can do. Each plugin defines its own nodes, for example, Essentials defines `essentials.fly` to toggle flight.

## Format

Nodes use lowercase letters, numbers, and dots as separators:

```text
essentials.fly
essentials.tpaccept
essentials.home
vault.economy.npc
worldguard.region.flag.<world>.<region>.<flag>
```

Some nodes contain placeholders like `<world>` or `<flag>` that you replace with actual values.

## Where to Use Them

Permission nodes can be assigned in `permissions.yml` or via commands:

### In permissions.yml

```yaml
groups:
  default:
    permissions:
      - essentials.help
      - essentials.list

  admin:
    permissions:
      - essentials.*
      - worldedit.*
```

### Via commands

```bash
/pex user <user> add <permission.node>
/pex group <group> add <permission.node>
```

## Finding Permission Nodes

Check the wiki or plugin documentation for the plugin you're using. You can also run `/pex user <user> check <permission.node>` to test whether a player has a specific node.
