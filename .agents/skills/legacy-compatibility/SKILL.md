---
name: legacy-compatibility
description: Use when changing original PermissionsEx API or command compatibility adapters.
---

# Legacy Compatibility

1. Identify the original PEX contract or command being preserved.
2. Compare signatures, argument handling, results, exceptions and side effects.
3. Delegate through the new API where possible.
4. Use Core-only hooks for compatibility mechanics; never leak them into the stable API.
5. Add regression tests for both successful and failing cases.
6. Run relevant adapter tests and Spigot/Paper smoke tests.

Do not modernise legacy behaviour unless the change is explicitly approved.
