---
sidebar_position: 2
---


# Storage Backends

PermissionsExPlus supports multiple storage backends. You can switch between them at runtime.

## Available Backends

| Backend | Type | Persistence | Best For |
|---------|------|-------------|----------|
| `file` | YAML files | Persistent | Most servers (default) |
| `h2` | H2 embedded database | Persistent | Medium servers |
| `sql` | MySQL / SQLite | Persistent | Large servers, external DB |
| `memory` | In-memory only | None | Testing, temporary setups |
| `multi` | Composite | Varies | Complex multi-backend setups |

## Changing the Backend

Edit `config.yml` to set your desired backend:

```yaml
permissions:
  backend: h2
  basedir: plugins/PermissionsEx
```

### File Backend (Default)

A simple YAML-based backend that stores all data in `permissions.yml`.

```yaml
permissions:
  backend: file
```

**Configuration:**
- `basedir`: directory where `permissions.yml` is stored

### H2 Backend

H2 is an embedded Java SQL database. It requires no external server and is a good choice for most servers.

```yaml
permissions:
  backend: h2
```

### SQL Backend

Supports both MySQL and SQLite.

```yaml
permissions:
  backend: sql
  basedir: plugins/PermissionsEx
```

**Configuration:**

```yaml
sql:
  driver: MySQL
  user: pex
  password: yourpassword
  host: localhost
  database: pex
  port: 3306
```

:::tip
For SQLite, omit the connection details and PEX will use a local file-based database.
:::

### Memory Backend

The memory backend stores everything in memory. Useful for testing or temporary setups.

:::caution
Data is lost when the server restarts. Do not use this backend for production.
:::

```yaml
permissions:
  backend: memory
```

### Multi Backend

The multi backend allows you to chain multiple backends together for advanced setups like master-slave replication or staged migration.

```yaml
permissions:
  backend: multi
multi:
  backends:
    - file
    - sql
```

## Switching Backends at Runtime

You can switch backends without restarting your server:

```bash
/pex backend h2
```

To migrate data between backends:

```bash
/pex import sql    # Import from SQL backend into the active backend
/pex import file   # Import from file backend into the active backend
```

:::tip
Always back up your permissions data before switching backends or performing imports.
:::
