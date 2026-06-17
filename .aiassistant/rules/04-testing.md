---
apply: always
---

## 11. Testing rules
- Add or update tests for behavior changes.
- Follow the project’s current testing style and file placement.
- Place tests in `{module}/src/test/java/` next to the code they exercise.
- Cover the happy path, important edge cases, and error paths affected by the change.
- Do not rewrite large areas of tests unless necessary.
- Use realistic fixtures and existing test helpers where available.
- Do not mock what the project normally tests end-to-end or integration-style unless there is a clear reason.

### Module test map
| Module | Typical focus |
|--------|---------------|
| `api-core`, `platform-api` | API SPI and runtime bridge unit tests |
| `legacy-api` | Frozen legacy contract (`LegacyApiContractTest`) |
| `common` | Engine, backends, commands, modern API integration |
| `bukkit`, `bungee`, `velocity`, `sponge` | Platform adapters and bridges |
| `proxy-common` | Shared proxy wiring |
| `universal` | Shaded jar contents (package jar first) |
| `example-plugin`, `example-legacy-plugin` | Hook plugin compile contracts |
| `legacy-compat` | MockBukkit hook smoke tests |

Run all tests: `mvn test` from repo root.

### Minimum test checklist
- New logic has tests.
- Changed behavior has updated tests.
- Important edge cases are covered.
- Error conditions are covered where relevant.
- Existing tests still reflect intended behavior.

## 17. Review checklist
Before finishing, confirm:
- Tests were added or updated where needed.
- Lint/format/type checks are satisfied where possible.