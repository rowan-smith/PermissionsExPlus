---
layout: default
title: Javadoc
permalink: /developers/reference/
description: Javadoc API reference for all PermissionsExPlus versions.
---

Browse generated API documentation. Method signatures, parameters, and return types are documented here — the [API Cookbook]({{ site.baseurl }}/developers/cookbook/) shows how to use them.

<div class="version-cards">
{% for doc in site.javadoc_versions %}
  <a class="version-card" href="{{ doc.url | prepend: site.baseurl }}">
    <span class="version-card-label">{{ doc.label }}</span>
    <span class="version-card-desc">{{ doc.description }}</span>
  </a>
{% endfor %}
</div>

## Key packages (1.23.5)

| API | Package | Entry point |
|-----|---------|-------------|
| **Modern service** | `dev.rono.permissions.api.service` | [PermissionsExApi]({{ site.baseurl }}/apidocs/1.23.5/dev/rono/permissions/api/service/PermissionsExApi.html) |
| **Subjects** | `dev.rono.permissions.api.subject` | [User]({{ site.baseurl }}/apidocs/1.23.5/dev/rono/permissions/api/subject/User.html), [Group]({{ site.baseurl }}/apidocs/1.23.5/dev/rono/permissions/api/subject/Group.html) |
| **Context** | `dev.rono.permissions.api.permission` | [PermissionContext]({{ site.baseurl }}/apidocs/1.23.5/dev/rono/permissions/api/permission/PermissionContext.html) |
| **Legacy** | `ru.tehkode.permissions` | [PermissionManager]({{ site.baseurl }}/apidocs/1.23.5/ru/tehkode/permissions/PermissionManager.html) |
| **Legacy events** | `ru.tehkode.permissions.events` | [PermissionEntityEvent]({{ site.baseurl }}/apidocs/1.23.5/ru/tehkode/permissions/events/PermissionEntityEvent.html) |

Versions **1.23.1 – 1.23.4** document the classic `ru.tehkode.*` API only. The modern `dev.rono.*` packages appear starting in **1.23.5**.

## Generate locally

**1.23.5** (PermissionsExPlus):

```bash
mvn -pl api/permissionsex-api,legacy-api/permissionsex-legacy-api javadoc:javadoc -am -Ddoclint=none
```

**Classic 1.23.x**:

```bash
./scripts/build-classic-javadoc.sh STABLE-1.23.4 1.23.4
```

## Maven coordinates

```xml
<groupId>dev.rono.permissions</groupId>
<artifactId>permissionsex-api</artifactId>
<version>{{ site.version }}</version>
<scope>provided</scope>
```
