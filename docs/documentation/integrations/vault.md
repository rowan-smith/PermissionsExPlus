# Vault

Vault is a common API used by many Bukkit plugins to interface with permissions and economy systems.

## Permissions Integration

PermissionsExPlus fully implements the Vault Permissions API. Any plugin that uses Vault will automatically be able to:

- Check player permissions.
- Get player groups.
- Check if a player is in a specific group.
- Get the primary group of a player.

## Chat Integration

PermissionsExPlus also implements the Vault Chat API, providing access to:

- Player and Group Prefixes.
- Player and Group Suffixes.
- Custom Options (metadata).

## Setup

No additional setup is required. PermissionsExPlus will automatically register itself with Vault if Vault is present on the server.

You can verify the integration by running:
```bash
/vault-info
```
It should show `PermissionsExPlus` as the active permission and chat provider.
