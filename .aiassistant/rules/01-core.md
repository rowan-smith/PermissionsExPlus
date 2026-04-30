---
apply: always
---

# AI Project Rules

This file defines the project-specific standards that AI coding assistants must follow.
Follow these instructions before introducing new code, changing architecture, or editing tests.

## 1. Operating principles
- Match the existing codebase before introducing a new pattern.
- Prefer the smallest correct change over a broad refactor.
- Reuse existing abstractions, utilities, and helpers before creating new ones.
- Do not invent architecture, folder structure, naming schemes, or dependencies.
- If the existing project contradicts a rule in this file, prefer the existing project pattern and call out the conflict clearly.
- Do not remove comments, docs, or TODOs unless the change makes them incorrect.
- Preserve user-visible behavior unless the task explicitly asks to change it.

## 2. Project context
- Tech stack: Java
- Main languages: Java
- App type: Plugin
- Primary frameworks/libraries: Spigot/Paper/Bukkit, Sponge, BungeeCord, Velocity
- Package manager/build tool: Maven
- Test framework: JUnit 5
- Lint/format tools: Spotless if configured, otherwise follow existing repository formatting

## 3. Codebase conventions
- Follow the current directory layout and module boundaries.
- Place new code in the closest existing feature/module, not in a new top-level folder.
- Keep files and classes focused on one responsibility.
- Prefer extending an existing file or module when the new logic clearly belongs there.
- Avoid creating helper files for one-off trivial logic.
- Use the project’s existing import style, export style, and dependency injection style.
- Prefer consistency with nearby code over personal preference.

## 4. Naming conventions
- Use the naming conventions already established in the repository.
- Do not mix naming styles within the same layer.
- Use descriptive names over abbreviated names unless abbreviations are already standard in the codebase.
- Use domain language consistently across services, models, DTOs, components, and tests.

### Default naming rules if the repo is unclear
- Classes/types/interfaces/records/annotations/enums: PascalCase
- Methods: camelCase
- Variables/fields/parameters: camelCase
- Constants: UPPER_SNAKE_CASE
- Enum values: UPPER_SNAKE_CASE
- Packages: all lowercase, dot-separated
- Files: match the public type name exactly
- Tests: mirror the source type name with a test suffix

## 6. Editing behavior
- Read surrounding code before editing.
- Match the style of the nearest comparable implementation.
- Change only what is necessary to complete the task safely.
- Avoid unrelated cleanup in the same change unless it blocks the task.
- If a refactor is necessary, keep it separate and explain why it is needed.
- Do not silently rename public APIs, commands, permission nodes, events, config keys, plugin channels, or storage fields.

## 7. Dependencies
- Prefer existing dependencies already in the project.
- Do not add a new package if the same outcome can be achieved with current libraries or standard library features.
- Ask before adding heavy, risky, or security-sensitive dependencies.
- When adding a dependency is necessary, explain why the current stack is insufficient.

## 8. Error handling
- Do not swallow exceptions.
- Use the project’s standard error-handling pattern.
- Return or throw errors with actionable context.
- Log errors only at the appropriate boundary; avoid duplicate logging across layers.
- Prefer explicit failure handling over silent fallbacks unless the codebase already uses graceful degradation for that case.

## 9. Validation and safety
- Validate external input at the boundary where the project expects validation.
- Assume all user input, network input, and file input may be malformed.
- Handle null, empty, missing, and invalid cases explicitly when relevant.
- Do not bypass auth, permission checks, or validation steps.
- Avoid leaking secrets, tokens, credentials, stack traces, or internal-only data.

## 10. Performance and scalability
- Avoid obviously wasteful work inside loops, repeated queries, or repeated renders.
- Prefer existing caching, batching, memoization, or pagination patterns already used in the project.
- Do not prematurely optimize, but do avoid known bad patterns.
- Call out performance tradeoffs when a change affects hot paths, rendering, DB access, or large collections.

## 13. Documentation updates
- Update docs, comments, config examples, or README sections when the change affects setup, behavior, or developer workflow.
- Keep docs concise and accurate.
- Do not add marketing language to technical docs.
- Remove or correct stale documentation introduced by the change.

## 14. Communication rules
- Be explicit about assumptions.
- Flag uncertainty instead of guessing.
- Summarize what changed, why, and any follow-up work.
- Mention important tradeoffs, migrations, or breaking changes clearly.
- If something needs team approval, say so directly.

## 15. Preferred coding style
- Prefer small, focused functions and methods.
- Prefer early returns over deeply nested conditionals where it improves clarity.
- Prefer straightforward code over clever abstractions.
- Prefer explicit types/contracts at boundaries when the language or framework benefits from them.
- Keep side effects contained and obvious.
- Keep commands, listeners, schedulers, and services focused on one responsibility each.
- Move reusable logic into services, managers, helpers, or dedicated utility classes only when that matches the existing codebase.

## 16. Anti-patterns to avoid
- Do not introduce broad refactors without being asked.
- Do not add “temporary” hacks without clearly marking them.
- Do not duplicate logic that already exists elsewhere.
- Do not mix unrelated concerns in one class, component, or module.
- Do not leave dead code, commented-out code, or placeholder implementations unless requested.
- Do not ignore failing tests, lint errors, or compiler warnings caused by the change.
- Do not invent configuration flags, environment variables, or magic constants without checking existing conventions.

## 17. Review checklist
Before finishing, confirm:
- The change follows nearby conventions.
- The smallest sensible change was made.
- Existing architecture was respected.
- New code is placed in the correct module/folder.
- Naming matches the repo style.
- Tests were added or updated where needed.
- Lint/format/type checks are satisfied where possible.
- Docs/config/examples were updated if behavior changed.