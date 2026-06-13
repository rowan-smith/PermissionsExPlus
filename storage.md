---
layout: default
title: Storage
permalink: /storage/
description: Where PermissionsExPlus stores config and permission data.
---

PermissionsExPlus stores its data in a folder on your server. By default this is:

```text
plugins/PermissionsEx/
├── config.yml
└── permissions.yml
```

## config.yml

Plugin settings — which backend to use, debug mode, and general behaviour. See [Configuration]({{ site.baseurl }}/configuration/).

## permissions.yml

Your groups, users, and permissions. This is the main file you edit when using the default **file** backend.

## Backends

PEX can store data in different ways:

| Backend | Best for |
|---------|----------|
| **file** (default) | Most servers — simple YAML files |
| **sql** | Large networks sharing one database |
| **memory** | Testing only — data is lost on restart |

Check your active backend:

```text
/pex backend
```

Switch backends:

```text
/pex backend sql
```

Import data from another backend:

```text
/pex import file
```

## UUID storage

Modern servers should use player UUIDs instead of usernames. Convert existing data:

```text
/pex convert uuid
```

## Reloading

After editing files manually, reload PEX:

```text
/pex reload
```

Or restart the server.
