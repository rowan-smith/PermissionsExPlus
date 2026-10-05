---
sidebar_position: 8
---

# Timed Permissions

Timed permissions expire after a specified duration. They work for both users and groups.

## Adding timed permissions

```bash
/pex user <user> timed add <permission> [lifetime] [world]
/pex group <group> timed add <permission> [lifetime] [world]
```

:::note
Lifetime formats: `30s`, `5m`, `2h`, `7d` (seconds, minutes, hours, days).
:::

## Removing timed permissions

```bash
/pex user <user> timed remove <permission> [world]
/pex group <group> timed remove <permission> [world]
```
