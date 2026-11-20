---
sidebar_position: 5
---

# Backend Errors

If you see errors related to storage backends (SQL, H2, file), work through these steps to diagnose the issue.

## 1. Check the active backend

Run `/pex backend` to see which backend is currently active:

```bash
/pex backend
```

## 2. Verify backend configuration

Check that your `config.yml` has the correct backend settings:

```bash
/pex config permissions.backend
```

For SQL backends, verify the connection details:

```bash
/pex config sql.host
/pex config sql.database
/pex config sql.user
```

## 3. Test database connectivity

For SQL and H2 backends, ensure the database server is running and accessible:

- **MySQL**: check that the MySQL service is running and the host/port are correct
- **SQLite / H2**: check that the data directory is writable
- **File**: check that `plugins/PermissionsEx/` is writable

## 4. Try the file backend as a fallback

If the database is unreachable, switch to the file backend to get your server running:

```bash
/pex backend file
```

This uses `permissions.yml` and requires no external database.

## 5. Check the console for errors

Enable debug mode to see detailed backend operations:

```bash
/pex toggle debug
/pex reload
```

Look for:
- Connection timeout errors (SQL/H2)
- File permission errors (file)
- Driver not found errors (SQL)
- Database locked errors (SQLite/H2)

## 6. Import data if switching backends

If you switched backends and need to restore data, use the import command:

```bash
/pex import file
```

:::tip
Always back up your `plugins/PermissionsEx/` directory before switching backends or importing data.
:::
