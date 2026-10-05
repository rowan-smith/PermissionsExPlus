---
sidebar_position: 1
---

# Requirements

## Server Software

PermissionsExPlus runs on any server software that implements the Bukkit/Spigot/Paper API:

- **Spigot**: 1.8.8 through 26.2
- **Paper**: 1.8.8 through 26.2
- **Purpur**: any version within the supported range
- **CraftBukkit**: may work but not recommended

:::caution
CraftBukkit may have compatibility issues. Spigot or Paper is recommended.
:::

:::note
Versions above 26.2 may work but are not tested. If you encounter issues, please [open an issue](https://github.com/rowan-smith/PermissionsExPlus/issues).
:::

## Java

| Server Version | Java Requirement |
|----------------|-----------------|
| 1.8.8 - 1.16   | Java 8+         |
| 1.17 - 1.20    | Java 17+        |
| 1.21+          | Java 21+        |

## Dependencies

:::tip
PermissionsExPlus has no external dependencies. All required libraries are bundled with the jar, just drop it in and restart.
:::

### Optional hook plugins

These plugins will be detected at runtime if present:

- **Vault**: for economy and permission hooks
- **PlaceholderAPI**: for placeholder expansion in prefixes, suffixes, groups, and more (see [PlaceholderAPI](../integrations/placeholder-api/overview))
