---
sidebar_position: 2
---

# Tab-Complete Not Working

If `/pex` command tab-completion isn't showing suggestions, work through these checks.

## Requirements

:::note
Tab-complete requires Bukkit/Spigot 1.13 or later. Older server software does not support command tab-completion for custom plugins.
:::

## Checks

### Did you type a space after `/pex`?

Tab-completion only triggers after the base command and a space. Type `/pex ` (with a space) and then press Tab.

### Do you have the required permission?

Tab-complete only suggests commands and arguments the player has permission to use. If you're missing `permissions.manage` or a more specific node, suggestions won't appear.

Check your permissions:

```bash
/pex user <yourname> list
```

### Are there errors in the server log?

Check the console for any errors when the plugin loads. A failed startup can prevent tab-complete from registering.

```bash
/pex reload
```

Then check the console for stack traces or error messages.

### Is the plugin loaded?

Run `/pex` with no arguments. If it shows the version banner, the plugin is loaded and the command is registered. If you get an "unknown command" error, the plugin didn't load correctly, check for dependency issues or startup errors.

## Still not working?

If tab-complete still doesn't function after the above checks, enable debug mode and try the command again:

```bash
/pex toggle debug
```

Check the console output for any permission resolution errors that might be blocking suggestions.
