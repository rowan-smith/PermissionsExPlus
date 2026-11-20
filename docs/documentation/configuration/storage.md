# Storage

PermissionsExPlus supports a wide range of storage backends.

## Supported Backends

- **Flat-file**: `yaml`, `json`
- **SQL**: `h2`, `sqlite`, `mysql`, `mariadb`, `postgresql`
- **Other**: `memory`, `redis` (sync only)

## Configuring Backends

Backends are configured in `database.yml`. 

Example for MySQL:
```yaml
type: mysql
address: localhost
port: 3306
database: pex
username: root
password: secret_password
```
