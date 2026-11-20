---
sidebar_position: 1
---

# Vault

PermissionsExPlus integrates with Vault to provide compatibility with economy, chat, and permission-using plugins. Vault support is declared as a soft dependency in plugin.yml, and PermissionsExPlus will register with Vault if it is present on the server.

## Features

- Exposes PermissionsExPlus as a Permissions provider for any Vault-compatible plugin
- Works with chat/economy plugins that use Vault for metadata and chat formatting

## Installation

1. Install [Vault](https://dev.bukkit.org/projects/vault) on your server.
2. Install PermissionsExPlus (Vault support is built-in).
3. Restart the server.

No additional configuration is required; PermissionsExPlus registers with Vault automatically when available.

:::caution
Vault is a soft dependency. Install Vault before starting the server so PermissionsExPlus can register as a provider. If PermissionsExPlus does not detect Vault, restart the server (avoid using /reload).
:::

:::note
Do not expose sensitive options or internal data through Vault-backed chat templates; only surface metadata that is safe for public display.
:::
