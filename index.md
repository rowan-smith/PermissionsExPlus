---
layout: page
title: Home
permalink: /
---

PermissionsExPlus is a maintained fork of the original [PermissionsEx](https://github.com/PEXPlugins/PermissionsEx) (PEX) plugin for Bukkit/Spigot servers. It keeps the familiar command structure and permission model while adding active maintenance, modern platform support, and updated compatibility.

**Current version:** `{{ site.version }}`

## What it does

PermissionsExPlus provides a flexible permissions system with support for:

- User and group permission management
- Group inheritance and hierarchy
- Prefix and suffix management
- Timed permissions and timed group membership
- Multi-world permission handling
- Rank ladder promotion and demotion
- Runtime backend inspection and switching
- UUID-based player records

## Supported platforms

| Platform | Status |
|----------|--------|
| Spigot / CraftBukkit | Supported |
| Paper | Supported |
| BungeeCord / Waterfall | Supported |
| Velocity | Supported |
| Sponge | Supported |

Install the universal **`PermissionsExPlus-{{ site.version }}.jar`** bootstrap artifact on each server process. See [Installation]({{ site.baseurl }}/installation/) for details.

## Quick start

```text
/pex group admin create
/pex group admin add '*'
/pex user Steve group set admin
/pex user Alex add essentials.home
/pex group moderator prefix [Mod]
/pex promote Steve
```

## Documentation

| Section | Description |
|---------|-------------|
| [Installation]({{ site.baseurl }}/installation/) | Download, build, and deploy the universal jar |
| [Commands]({{ site.baseurl }}/commands/) | Full `/pex` command reference |
| [Examples]({{ site.baseurl }}/examples/) | Sample configs and common setups |
| [Compatibility]({{ site.baseurl }}/compatibility/) | Minecraft, Java, and platform matrix |
| [Integrations]({{ site.baseurl }}/integrations/) | Hook plugin development guide |
| [Modern API]({{ site.baseurl }}/modern-api/) | `dev.rono.permissions.api` reference |
| [Legacy API]({{ site.baseurl }}/legacy-api/) | Classic `ru.tehkode.permissions` reference |
| [Architecture]({{ site.baseurl }}/architecture/) | Module layout and design rules |

## Why this fork exists

PermissionsEx was widely used, but the original project became unmaintained. PermissionsExPlus continues that legacy with active fixes, updated compatibility, and a clearer long-term home for the plugin.

## Credits

- Original authors: `t3hk0d3`, `zml`
- Fork maintenance: `Rono` / [rowan-smith](https://github.com/rowan-smith)
- License: [GNU GPL v2.0 or later](https://github.com/{{ site.repo }}/blob/main/LICENSE)
