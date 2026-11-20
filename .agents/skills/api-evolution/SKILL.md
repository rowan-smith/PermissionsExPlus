---
name: api-evolution
description: Use when adding or changing PermissionsExPlus public API contracts, managers, events, models or resolvers.
---

# API Evolution

1. Identify existing public signatures and expected behaviour.
2. Keep all types and dependencies platform-independent.
3. Prefer additive changes; check source and binary compatibility.
4. Keep implementation-only and legacy-only features outside the public API.
5. Document nullability, threading and asynchronous completion semantics.
6. Add API contract tests and run compatibility checks.

Do not change a public contract just to simplify one platform implementation.
