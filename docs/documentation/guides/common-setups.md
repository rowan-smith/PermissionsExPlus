# Common Setups

## Basic Survival Server

- **Groups**: `member`, `vip`, `admin`
- **Inheritance**: `admin` -> `vip` -> `member`
- **Permissions**:
  - `member`: `essentials.spawn`, `essentials.home`
  - `vip`: `essentials.fly`, `member`
  - `admin`: `*`

## BungeeCord/Velocity Network

- **Backends**: MySQL or PostgreSQL for global storage.
- **Contexts**: Use `--server survival` or `--server lobby` to differentiate permissions across the network.
- **Sync**: Enable Redis for instant permission updates.
