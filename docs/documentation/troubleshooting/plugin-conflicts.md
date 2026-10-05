---
sidebar_position: 6
---

# Plugin Conflicts

PermissionsExPlus manages Bukkit's permission system. Other plugins that also modify permissions can cause conflicts, unexpected behavior, or data corruption.

## Known Conflicting Plugins

Do not run these alongside PermissionsExPlus:

- **GroupManager**: another permissions management plugin
- **LuckPerms**: another permissions management plugin
- **zPermissions**: another permissions management plugin
- **bPermissions**: another permissions management plugin
- **Any plugin that calls `Player.addAttachment()` or directly modifies Bukkit permissions**: these bypass PermissionsExPlus entirely

## How to Identify a Conflict

If permissions behave erratically (granting/denying at random, not respecting groups, or resetting after reload), a conflict is likely.

### Check for other permissions plugins

List all installed plugins:

```bash
/plugins
```

Look for any other permissions management plugins. Only one should be installed.

### Check for permission overrides

Some plugins hook into Bukkit's permission API and override values. Enable debug mode and watch for permission checks that come from unexpected sources:

```bash
/pex toggle debug
```

Then trigger the problematic permission check and review the console output.

## Resolving Conflicts

1. **Remove the conflicting plugin**: the cleanest solution. Only one permissions plugin should manage permissions.
2. **Disable the conflicting feature**: some plugins have config options to disable their permissions hooks. Check the other plugin's documentation.
3. **Check plugin load order**: if both plugins must coexist, ensure PermissionsExPlus loads last so it takes priority. Use `softdepend` or `loadafter` in your plugin configuration.

## Preventing Future Conflicts

When installing new plugins, check their description for permissions-related features. If a plugin claims to "manage permissions" or "hook into Vault permissions," test it carefully before deploying to production.
