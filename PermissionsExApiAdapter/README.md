# PermissionsEx Api Adapter

## Binary Compatability Shim

* Provides a 100% binary api compatability shim for the original PermissionsEx API.
* This adapter allows for seamless integration with plugins that rely on the PermissionsEx API, ensuring compatibility with existing configurations and permissions systems.

### How it works

We delegate all API calls to the PermissionsExPlus API equivilents.

### Differences

* We have stripped out:
  * Configuration (handled by PermissionsExPlus)
  * Commands (original commands are handled by PermissionsExCommandAdapter)
  * Backends (handled by PermissionsExPlus)

* Configuration is handled by the PermissionsExPlus API.
  * This will include a `/pex backend import permissionsex` command or delegate in future.

## Public API contract tests

Behavioral and binary-surface contracts live under `src/test/java/ru/tehkode/permissions/contract/`.
The binary baseline is classic **PermissionsEx 1.23.5** (`STABLE-1.23.5`), not the live adapter:

* `baselines/PermissionsEx-1.23.5-api.jar` - classes-only JAR from that tag (source of truth)
* `PublicApiBinaryContractTest` - asserts ApiAdapter still exposes every public member from 1.23.5;
  additive shim members are allowed. Also checks the committed signature dump matches the JAR.
* `LegacyPublicApiContractTest` - semantics plugins rely on
* `PermissionsDataAndBackendContractTest` / `PermissionsExFacadeMemberContractTest` - SPI + facade

Regenerate the human-readable dump from the 1.23.5 JAR (never from the adapter):

```shell
mvn -pl PermissionsExApiAdapter test -Dtest=PublicApiBinaryContractTest -Dpex.contracts.update=true
```

Run only the contract suite:

```shell
mvn -pl PermissionsExApiAdapter test -Dtest=ru.tehkode.permissions.contract.**
```

See `src/test/resources/baselines/README.md` to rebuild the baseline JAR from the tag.

## Implementation
- The api adapter imports `PermissionsExPlus/Core` directly so it can
  use `PexImplProvider.get()` essentially using an **unsupported** api.

## Hard Dependencies

- PermissionExPlus
