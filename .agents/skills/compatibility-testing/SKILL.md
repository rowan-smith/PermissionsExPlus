---
name: compatibility-testing
description: Use when fixing permission bugs, changing storage or commands, or validating cross-version and cross-platform behaviour.
---

# Compatibility Testing

1. State the expected behaviour and affected modules/versions.
2. Add focused unit tests for resolution, context, inheritance and edge cases.
3. Add integration tests for persistence, cache invalidation and events when relevant.
4. Use real-server RCON and player tests for commands, plugin lifecycle and permission checks.
5. Check legacy API/command compatibility where applicable.
6. Run Maven verify and the relevant CI smoke matrix; report skipped coverage.

Prefer regression tests that reproduce the original failure before the fix.
