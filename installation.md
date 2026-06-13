---
layout: page
title: Installation
permalink: /installation/
---

## Requirements

| Requirement | Detail |
|-------------|--------|
| **Minecraft** | Target range **1.8.8 – 1.26.1** on Spigot/Paper and compatible forks |
| **Java** | **Java 21+** required to run this build (bytecode is Java 21) |
| **Proxies** | BungeeCord, Waterfall, Velocity supported via the same universal jar |

See [Compatibility]({{ site.baseurl }}/compatibility/) for full platform notes.

## Download or build

### Option 1: Release jar (recommended)

Download **`PermissionsExPlus-{{ site.version }}.jar`** from [GitHub Releases](https://github.com/{{ site.repo }}/releases) when available.

### Option 2: Build from source

```bash
git clone https://github.com/{{ site.repo }}.git
cd permissionsexplus
mvn clean package -pl bootstrap -am
```

Output: **`bootstrap/target/PermissionsExPlus-{{ site.version }}.jar`**

## Install on a server

1. **Remove conflicting jars** from `plugins/` (or Sponge `mods/`):
   - `permissionsex-spigot-*.jar`, `permissionsex-bungee-*.jar`
   - Older `PermissionsExPlus-spigot-*.jar`, `PermissionsExPlus-bungee-*.jar`
   - Legacy `ru.tehkode:permissionsex-*` coordinates
2. Copy **`PermissionsExPlus-{{ site.version }}.jar`** into `plugins/` on each backend and proxy.
3. Start or restart the server.
4. Configure groups and users via [commands]({{ site.baseurl }}/commands/) or [configuration files]({{ site.baseurl }}/configuration/).

**Important:** Each server process must have only **one** PermissionsExPlus jar.

## Universal jar routing

The bootstrap jar contains descriptors for every supported platform. Each loader reads only its own descriptor:

| Platform | Descriptor | Main class |
|----------|------------|------------|
| Spigot / CraftBukkit | `plugin.yml` | `ru.tehkode.permissions.bukkit.PermissionsEx` |
| Paper | `paper-plugin.yml` | `dev.rono.permissions.paper.PaperPermissionsExPlugin` |
| BungeeCord / Waterfall | `bungee.yml` | `dev.rono.permissions.bungee.BungeePermissionsExPlugin` |
| Velocity | `velocity-plugin.json` | `dev.rono.permissions.velocity.VelocityPermissionsExPlugin` |
| Sponge | `META-INF/sponge_plugins.json` | `dev.rono.permissions.sponge.SpongePermissionsExPlugin` |

## First-run configuration

On first start, PEX creates a config directory (default: `plugins/PermissionsEx/`):

```
plugins/PermissionsEx/
├── config.yml
└── permissions.yml
```

See [Configuration]({{ site.baseurl }}/configuration/) and [Examples]({{ site.baseurl }}/examples/) for starter files.

## Verify installation

```text
/pex
/pex backend
/pex groups list
```

If commands respond with PEX help output, the plugin loaded successfully.

## Hook plugin development

If you are writing a companion plugin that integrates with PEX, see [Integrations]({{ site.baseurl }}/integrations/). You do **not** need to install additional PEX jars — only add Maven `provided` dependencies at compile time.
