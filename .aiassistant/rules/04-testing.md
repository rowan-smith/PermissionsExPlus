---
apply: always
---

## 11. Testing rules
- Add or update tests for behavior changes.
- Follow the project’s current testing style and file placement.
- Cover the happy path, important edge cases, and error paths affected by the change.
- Do not rewrite large areas of tests unless necessary.
- Use realistic fixtures and existing test helpers where available.
- Do not mock what the project normally tests end-to-end or integration-style unless there is a clear reason.

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