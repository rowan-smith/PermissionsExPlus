---
sidebar_position: 2
---

# Installation

This guide covers installing PermissionsExPlus on a Bukkit-compatible Minecraft server.

:::info
Before installing, review the [Requirements](requirements) page to make sure your server version and Java version are supported.
:::

## Download PermissionsExPlus

Download the latest PermissionsExPlus jar from the [GitHub releases page](https://github.com/rowan-smith/PermissionsExPlus/releases).

Use a stable release unless you specifically need a development build.

## Install the Plugin

1. Stop your Minecraft server.
2. Place the PermissionsExPlus jar in your server's `plugins/` directory.
3. Install any optional integration plugins you intend to use.
4. Start the server normally.

Your server directory should look similar to this:

```text
server/
├── plugins/
│   └── PermissionsExPlus.jar
├── server.properties
└── server.jar
```

:::caution
Use a full server restart when installing or updating PermissionsExPlus. Avoid using `/reload`, as it can leave plugins and integrations in an inconsistent state.
:::

## Optional Integrations

PermissionsExPlus has no required external dependencies. All libraries required by the plugin are included in its jar.

The following plugins provide optional functionality:

| Plugin                                                     | Purpose                                                                                 |
|------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| [Vault](../integrations/vault)                             | Allows compatible plugins to access permissions, prefixes and suffixes                  |
| [PlaceholderAPI](../integrations/placeholder-api/overview) | Provides user and group placeholders for chat, tab lists, scoreboards and other plugins |

Install optional integration plugins before starting the server so PermissionsExPlus can detect and register with them.

## Verify the Installation

After the server has started, run:

```bash
/pex version
```

You can also run:

```bash
/pex
```

PermissionsExPlus should display its version information.

You can confirm that the plugin is loaded with:

```bash
/plugins
```

PermissionsExPlus should appear in the installed plugin list.

## Generated Files

On its first successful startup, PermissionsExPlus creates its data directory:

```text
plugins/PermissionsEx/
```

The primary files are:

| File              | Purpose                                         |
|-------------------|-------------------------------------------------|
| `config.yml`      | Core plugin settings and storage configuration  |
| `permissions.yml` | User and group data when using the file backend |

Your plugin directory should then look similar to this:

```text
plugins/
├── PermissionsEx.jar
└── PermissionsEx/
    ├── config.yml
    └── permissions.yml
```

:::note
The data directory retains the original `PermissionsEx` name for compatibility.
:::

## Installation Problems

### The plugin does not appear in `/plugins`

Check the server console for startup errors. Common causes include:

- An unsupported server version
- An incompatible Java version
- An incomplete or corrupted jar download
- The jar being placed in the wrong directory
- Another permissions manager interfering with startup

See [Requirements](requirements) for supported environments.

### `/pex` is an unknown command

Check that PermissionsExPlus loaded successfully. If startup failed, the `/pex` command will not be registered.

Review the server console for the original error rather than repeatedly restarting the server.

### Vault or PlaceholderAPI is not detected

Make sure the integration plugin is installed and loaded, then perform a full server restart.

Do not rely on `/reload` to register integrations.

## Next Step

Continue to [Quick Start](quick-start) to create your first groups, assign permissions and give yourself administrator access.
