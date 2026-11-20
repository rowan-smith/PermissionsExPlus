---
sidebar_position: 10
---

# Options

Options are key-value pairs used for metadata like prefixes, suffixes, and custom data. They can be set globally or per-world.

## Configuration

```yaml
users:
  Steve:
    prefix: "&c"                  # Red chat prefix
    suffix: "&r"                  # Reset formatting after name
    options:
      color: red                  # Custom option for chat plugins
    worlds:
      creative:
        options:
          fly: true               # Only enable fly in creative world
```

## Accessing options

```bash
/pex user <user> get prefix
/pex user <user> get color creative
/pex user <user> set prefix "&c"
/pex group <moderator> set prefix "&6[Mod] "
```

## Common options

| Option | Description |
|--------|-------------|
| `prefix` | Chat prefix displayed before the player name |
| `suffix` | Chat suffix displayed after the player name |
| `color` | Primary color code for chat formatting |
| `fly` | Enable flight (used by Essentials and similar plugins) |
