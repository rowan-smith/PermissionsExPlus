# PermissionsEx Api Adapter

## Binary Compatibility Shim

Thin binary-compat layer over PermissionsExPlus for the original PermissionsEx
API (`ru.tehkode.permissions.*`). Public types and method signatures remain;
behavior is forwarded to Plus.

### How it works

| Legacy call                                 | Plus target                                  |
|---------------------------------------------|----------------------------------------------|
| `has` / inherited options / prefix / suffix | `PexImplProvider.get().resolvers()…`         |
| mutate permissions / options / parents      | `users()/groups().modify(…)` via data bridge |
| promote / demote                            | `ladders().promote/demote`                   |
| config getters                              | Plus config facade (`PermissionsExConfig`)   |

The live backend is always `data`. Legacy aliases (`file`, `sql`, `memory`,
`multi`) remain as public types and redirect to the same bridge.

### Configuration & storage

Owned entirely by PermissionsExPlus (`config.yml`, `advanced.yml`,
`database.yml`). The adapter has no independent stores or config file.

### Differences from PermissionsEx 1.x

- Commands: `PermissionsExCommandAdapter` / Plus Cloud commands
- Bukkit injection: PermissionsExPlus (legacy Superperms types kept for ABI only)
- Resolution engine: PermissionsExPlus (not local HierarchyTraverser)

## Public API contract tests

Behavioral and binary-surface contracts live under `src/test/java/ru/tehkode/permissions/contract/`.
The binary baseline is classic **PermissionsEx 1.23.5** (`STABLE-1.23.5`), not the live adapter:

* `baselines/PermissionsEx-1.23.5-api.jar` — classes-only JAR from that tag (source of truth)
* `PublicApiBinaryContractTest` — asserts ApiAdapter still exposes every public member from 1.23.5;
  additive shim members are allowed. Also checks the committed signature dump matches the JAR.
* `LegacyPublicApiContractTest` — semantics plugins rely on
* `PermissionsDataAndBackendContractTest` / `PermissionsExFacadeMemberContractTest` — SPI + facade

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

Uses `PexImplProvider.get()` for operations not yet on public `PexApi`
(`loadOrCreateUser`, create/delete). Prefer public `PexApi` where available.

## Hard Dependencies

- PermissionsExPlus
