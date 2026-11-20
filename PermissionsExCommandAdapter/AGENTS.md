# Legacy PermissionsEx Command Adapter

- Preserve original `/pex` command names, aliases, argument order, permissions and observable behaviour.
- This adapter runs only with the Spigot/Paper compatibility integration.
- Keep legacy command parsing and output separate from the modern command framework.
- Use implementation-only hooks to disable or replace modern command registration when required.
- Never add command-adapter control methods to the stable public API.
- Test success, error, tab-completion and permission-denied cases against legacy expectations.
- Verify registration and dispatch on real servers, not only unit mocks.
