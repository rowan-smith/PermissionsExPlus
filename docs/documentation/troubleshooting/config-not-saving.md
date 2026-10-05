---
sidebar_position: 3
---

# Config Changes Not Saving

If you've edited `config.yml` or `permissions.yml` and the changes don't seem to take effect, work through these steps.

## 1. Check directory permissions

The `plugins/PermissionsEx/` directory must be writable by the server process. If the server runs as a specific user (e.g. `minecraft`), that user needs write access.

### Linux

```bash
ls -la plugins/PermissionsEx/
```

Look for write permissions on `config.yml` and `permissions.yml`. If needed:

```bash
chmod -R u+w plugins/PermissionsEx/
```

### Windows

Right-click the `PermissionsEx` folder, Properties, Security, ensure the server user has Modify access.

## 2. Reload after editing

After making manual edits, run `/pex reload` to force the plugin to re-read from disk:

```bash
/pex reload
```

Without this, the plugin may continue using its in-memory copy.

## 3. Check for file lock issues

Some server setups (particularly Windows) can lock files while the plugin is running. Try:

1. Stop the server
2. Edit the file directly
3. Restart the server

## 4. Use commands instead

As an alternative to editing files manually, use the `/pex config` command to read and modify values at runtime:

```bash
/pex config permissions.backend h2
/pex config permissions.debug true
```

Changes made via `/pex config` are saved to disk immediately.

## 5. Check console logs

Enable debug mode and reload to see detailed file I/O logs:

```bash
/pex toggle debug
/pex reload
```

Look for error messages about file access, permission denied, or I/O exceptions.
