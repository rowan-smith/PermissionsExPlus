---
layout: page
title: Developer Resources
permalink: /developer-resources/
---

Resources for building PermissionsExPlus from source and developing hook plugins.

## Repository

- **GitHub:** [github.com/{{ site.repo }}](https://github.com/{{ site.repo }})
- **Version:** `{{ site.version }}`
- **Maven group:** `dev.rono.permissions`

## Build from source

```bash
git clone https://github.com/{{ site.repo }}.git
cd permissionsexplus
mvn clean package
```

### Universal jar only

```bash
mvn clean package -pl bootstrap -am
```

Output: `bootstrap/target/PermissionsExPlus-{{ site.version }}.jar`

### Run tests

```bash
mvn test
```

Modern API integration tests:

```bash
mvn -pl common/permissionsex-core test \
  -Dtest='ModernApi*Test,ApiLayerInvariantTest,WorldsTest,PermissionContextTest'
```

## Maven artifacts for hook plugins

| Artifact | Scope | Purpose |
|----------|-------|---------|
| `permissionsex-api` | `provided` | Modern hook surface |
| `permissionsex-core-api` | `provided` | Platform host / bus types (advanced) |
| `permissionsex-legacy-api` | `provided` | Classic types and events |
| `permissionsex-legacy-stub` | `provided` | `PermissionsEx` static helpers (compile-only) |

Example `pom.xml` dependency:

```xml
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-api</artifactId>
  <version>{{ site.version }}</version>
  <scope>provided</scope>
</dependency>
```

## Generate API Javadoc

From the repository root:

```bash
# Unified docs for core-api + permissionsex-api
mvn -pl api javadoc:aggregate -am

# Single-module docs
mvn -pl api/permissionsex-api javadoc:javadoc -am
```

Output:

- `api/target/reports/apidocs/` (aggregate)
- `api/permissionsex-api/target/reports/apidocs/` (single module)

## Hosted Javadoc

This site includes archived Javadoc from the original PermissionsEx project:

- [Javadoc 1.22.1]({{ site.baseurl }}/apidocs/1.22.1/apidocs/index.html)
- [Javadoc 2.0-SNAPSHOT]({{ site.baseurl }}/apidocs/2.0-SNAPSHOT/apidocs/index.html) (historical)

For PermissionsExPlus-specific API reference, prefer the markdown docs:

- [Modern API]({{ site.baseurl }}/modern-api/)
- [Legacy API]({{ site.baseurl }}/legacy-api/)

## Module map

| Group | Key modules |
|-------|-------------|
| `legacy-api` | `permissionsex-legacy-api`, `permissionsex-legacy-stub`, `permissionsex-legacy-compat` |
| `api` | `permissionsex-core-api`, `permissionsex-api` |
| `common` | `permissionsex-platform-api`, `permissionsex-core` |
| `platform` | `permissionsex-spigot`, `permissionsex-bungee`, `permissionsex-paper`, `permissionsex-velocity`, `permissionsex-sponge` |
| `bootstrap` | `permissionsex-bootstrap` |
| `plugin` | Example hook plugins |

See [Architecture]({{ site.baseurl }}/architecture/) for the full module diagram.

## Contributing

Contributions, bug reports, and compatibility fixes are welcome.

1. Fork the repository
2. Create a feature branch
3. Run `mvn test`
4. Open a pull request with a clear description

## License

PermissionsExPlus is licensed under the [GNU General Public License v2.0 or later](https://github.com/{{ site.repo }}/blob/main/LICENSE).

## Credits

- Original authors: `t3hk0d3`, `zml`
- Fork maintenance: `Rono` / [rowan-smith](https://github.com/rowan-smith)
