---
sidebar_position: 1
---

# Configuration File

PermissionsExPlus stores its main configuration in `config.yml` inside the plugin data folder.

```text
plugins/PermissionsEx/config.yml
```

The file is created automatically on first startup. You can edit it directly or use the `/pex config` command to read and modify values at runtime.

## Full Configuration

```yaml
permissions:
  # The storage backend to use (file, sql, h2, memory, multi)
  backend: h2

  # Base directory for plugin data
  basedir: plugins/PermissionsEx

  # Enable debug mode for verbose permission resolution logging
  debug: false

  # Allow server operators to bypass permissions
  allowOps: false

  # When adding a user to a group, process group permissions last
  user-add-groups-last: false

  # Log player login events
  log-players: false

  # Automatically create user records on first join
  createUserRecords: false

  # Save the default group flag with user data
  save-default-group: false

  # Notify players when their permissions change
  informplayers:
    changes: false

# SQL backend configuration (only used when backend is set to sql)
sql:
  driver: MySQL
  user: pex
  password: yourpassword
  host: localhost
  database: pex
  port: 3306

# Multi backend configuration (only used when backend is set to multi)
multi:
  backends:
    - file
    - sql
```

## Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `permissions.backend` | `h2` | Storage backend: `file`, `sql`, `h2`, `memory`, or `multi` |
| `permissions.basedir` | `plugins/PermissionsEx` | Base directory for plugin data files |
| `permissions.debug` | `false` | Enable verbose debug logging for permission resolution |
| `permissions.allowOps` | `false` | Allow server operators to bypass all permission checks |
| `permissions.user-add-groups-last` | `false` | Process group assignment after other permission operations |
| `permissions.log-players` | `false` | Log player join/leave events |
| `permissions.createUserRecords` | `false` | Create user records automatically on first join |
| `permissions.save-default-group` | `false` | Persist the default group flag with user data |
| `permissions.informplayers.changes` | `false` | Notify players when their permissions are modified |

:::warning
Enabling `permissions.allowOps` bypasses all permission checks for server operators. Only use this if you understand the security implications.
:::

:::note
When `permissions.createUserRecords` is `true`, new users are automatically registered on first join. This may not be expected for all setups.
:::

## Managing Configuration via Commands

### View a config value

```bash
/pex config <node>
```

Prints the current value of a configuration node.

**Examples:**

```bash
/pex config permissions.backend
/pex config permissions.debug
/pex config sql.host
```

### Set a config value

```bash
/pex config <node> [value]
```

Sets a configuration node to a new value and saves the file.

**Examples:**

```bash
/pex config permissions.debug true
/pex config permissions.backend sql
/pex config sql.host localhost
/pex config sql.password mypassword
```

:::warning
Changes to `permissions.backend` require a reload or server restart to take effect. Use `/pex backend <backend>` to switch backends at runtime without editing the file.
:::

### Reload configuration

```bash
/pex reload
```

Reloads the plugin configuration and reinitializes all backends. Use this after making manual edits to `config.yml`.

### Print the hierarchy

```bash
/pex hierarchy [world]
```

Prints the complete user/group inheritance hierarchy, useful for verifying your configuration is working as expected.

### Toggle debug mode

```bash
/pex toggle debug
```

Enables or disables debug mode at runtime. When enabled, detailed permission resolution logs are printed to the console. This overrides the `permissions.debug` config value until the server restarts.

## Backend-Specific Configuration

See [Storage Backends](storage) for detailed configuration of each backend type, including SQL connection settings and multi-backend setups.
