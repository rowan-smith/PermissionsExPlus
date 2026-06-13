---
layout: page
title: About
permalink: /about/
---

## PermissionsExPlus

PermissionsExPlus is a maintained fork of the original [PermissionsEx](https://github.com/PEXPlugins/PermissionsEx) plugin for Bukkit/Spigot servers.

The goal of this fork is to keep PermissionsEx usable on modern server environments, preserve the familiar command structure, and continue maintenance for server administrators who still rely on PEX-style permission management.

## Why this fork exists

PermissionsEx was one of the most widely used permission plugins for Minecraft servers. After the original project became unmaintained, PermissionsExPlus was created to:

- Fix compatibility issues on modern Spigot/Paper, Bungee, Velocity, and Sponge
- Restore and freeze the classic `ru.tehkode.*` hook plugin API
- Introduce a modern `dev.rono.permissions.api` integration surface
- Provide a universal bootstrap jar for multi-platform deployments
- Maintain active development and documentation

## Current release

**Version {{ site.version }}**

| Area | State |
|------|--------|
| Build | Full reactor tests pass |
| Spigot/Paper | Compiles against 1.21.x API |
| Bungee | Compiles and tests against BungeeCord API |
| Legacy hook plugins | Contract restored to baseline `628215f` |
| Minecraft target | 1.8.8 – 1.26.1 (Java 21 runtime required) |

## Credits

- **Original authors:** `t3hk0d3`, `zml`
- **Fork maintenance:** `Rono` / [rowan-smith](https://github.com/rowan-smith)
- **Original project:** [PermissionsEx](https://github.com/PEXPlugins/PermissionsEx)

## License

PermissionsExPlus is licensed under the **GNU General Public License v2.0 or later**.

See the [LICENSE](https://github.com/{{ site.repo }}/blob/main/LICENSE) file in the repository.

## Links

- [GitHub Repository](https://github.com/{{ site.repo }})
- [Documentation Home]({{ site.baseurl }}/)
- [Original PermissionsEx](https://github.com/PEXPlugins/PermissionsEx)
