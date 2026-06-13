---
apply: always
---

## 5. Architecture rules
- Follow the existing plugin package structure, such as command, listener, service, manager, config, storage, and util packages.
- Keep the main plugin class small and limited to bootstrap, registration, and lifecycle wiring.
- Put gameplay logic in dedicated services/managers, not directly in command executors or event listeners.
- Keep command classes focused on argument parsing, permission checks, and delegation.
- Keep event listeners focused on filtering, validation, and delegation.
- Put persistence, file I/O, and external API access in dedicated services.
- Avoid introducing new architectural patterns or abstraction layers unless they match the existing plugin structure.
- Keep public plugin APIs, commands, permissions, and config keys stable unless the task explicitly requires a breaking change.

## 18. Examples of good instructions

### Example: add a command
Good:
- Add the command using the existing command package and registration pattern.
- Keep permission checks and argument parsing in the command class.
- Delegate gameplay logic to an existing or new service if needed.
- Add or update tests for parsing and business logic where the project supports it.
- Reuse existing message/config patterns.

Bad:
- Put all gameplay logic directly in the command executor.
- Hardcode messages that should come from config.
- Introduce a new command framework for one command.

### Example: add an event listener
Good:
- Register the listener using the existing plugin bootstrap pattern.
- Keep the listener focused on filtering and delegation.
- Move expensive work out of the event handler.
- If async work is needed, return to the main thread before touching unsafe API state.

Bad:
- Perform file or database I/O directly in the event handler.
- Put business rules, persistence, and messaging all in one listener class.
- Access Bukkit world/entity state from async code without platform support.

### Example: fix a plugin bug
Good:
- Reproduce the issue from the command, event, or scheduler path involved.
- Fix the smallest root cause.
- Add a regression test where practical.
- Preserve command names, permissions, config keys, and public behavior unless the task requires a breaking change.

Bad:
- Rewrite the plugin structure for a small bug.
- Move unrelated commands or listeners while fixing the issue.
- Patch around the symptom without understanding the event or scheduler flow.

## 19. Stack-specific optional sections

### Minecraft / plugin projects
- Follow the existing command/listener/service structure.
- Keep plugin lifecycle code small and delegate logic to services/managers.
- Do not hardcode config-driven values if the project already uses config files.
- Respect threading and scheduler rules of the platform.
- Avoid doing blocking work on the main server thread.

## Threading and scheduler rules
- Do not perform blocking I/O, database access, web requests, or heavy file operations on the main server thread.
- Use asynchronous tasks for slow I/O or expensive computation when the platform allows it.
- Do not call thread-unsafe Bukkit/Spigot API methods from asynchronous tasks unless the API explicitly supports it.
- If async work needs to affect the server state, schedule the result back onto the main thread.
- Prefer short synchronous tasks over long-running tick work.
- If work must be split across ticks, batch it into small scheduled units rather than freezing the server.

## Configuration and lifecycle
- Prefer config-driven values over hardcoded messages, limits, toggles, and timings when the project already uses config files.
- Reuse the existing config access pattern and config key naming style.
- Validate config values when loading or reloading them.
- Keep onLoad, onEnable, and onDisable small and predictable.
- Register commands, listeners, tasks, and services during lifecycle setup using the project’s existing pattern.
- Clean up scheduled tasks, open resources, and external connections during plugin shutdown.