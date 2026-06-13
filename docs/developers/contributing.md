---
title: Contributing
description: How to contribute to PermissionsExPlus.
slug: /developers/contributing
---

Contributions are welcome — bug reports, compatibility fixes, and documentation improvements.

## Report a bug

1. Check [existing issues](https://github.com/%%site.repo%%/issues)
2. Use `/pex report` in-game to generate diagnostic info
3. [Open a new issue](https://github.com/%%site.repo%%/issues/new) with:
   - Server type and version (Spigot, Paper, etc.)
   - Minecraft version
   - Java version
   - Steps to reproduce

## Build from source

```bash
git clone https://github.com/%%site.repo%%.git
cd PermissionsExPlus
mvn clean package
```

Universal jar:

```bash
mvn clean package -pl bootstrap -am
```

Output: `bootstrap/target/PermissionsExPlus-%%site.version%%.jar`

See [Universal Bootstrap Jar](/developers/bootstrap) for install and routing details.

## Run tests

```bash
mvn test
```

Pre-release verification: [Real-Server Test Matrix](/developers/testing-matrix).

## Submit a pull request

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run `mvn test`
5. Open a PR with a clear description of what changed and why

## Code guidelines

- Match existing code style in the module you are editing
- Keep platform modules thin — permission logic belongs in `permissionsex-core`
- New features go in the modern API (`dev.rono.permissions.api`), not the frozen legacy API
- Add tests for behaviour changes where practical

## License

By contributing, you agree that your contributions are licensed under the [GPL v2.0 or later](https://github.com/%%site.repo%%/blob/main/LICENSE).
