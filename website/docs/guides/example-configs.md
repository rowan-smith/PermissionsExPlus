---
title: Example configuration files
description: Starter config.yml and sample permissions data for PermissionsExPlus.
slug: /guides/example-configs
---

Starter configuration files live in the repository [`examples/`](https://github.com/rowan-smith/PermissionsExPlus/tree/main/examples) directory at the project root.

## config.yml

[`examples/config.yml`](https://github.com/rowan-smith/PermissionsExPlus/blob/main/examples/config.yml) — copy to `plugins/PermissionsEx/config.yml` on your server.

Uses the default **`h2`** H2 backend with `command-framework: modern`. See [Configuration](/configuration/) for all options.

## permissions.yml

[`examples/permissions.yml`](https://github.com/rowan-smith/PermissionsExPlus/blob/main/examples/permissions.yml) — sample groups and users.

With the default **`h2`** backend, place this file at `plugins/PermissionsEx/permissions.yml` before first startup. PEX imports it into H2 (`permissions.mv.db`) and renames the file to `permissions.yml.migrated`. You can also use it as a reference when preparing a `file` backend section for manual import.

See also the [Configuration](/configuration) page for option reference.
