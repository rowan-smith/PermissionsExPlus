# Troubleshooting

If something isn't working, check the relevant guide below.

## Common Issues

- [Permissions Not Applying](./permissions-not-applying): a player's permissions don't seem to take effect
- [Tab-Complete Not Working](./tab-complete): `/pex` command suggestions aren't appearing
- [Config Changes Not Saving](./config-not-saving): edits to config.yml or permissions.yml don't persist
- [UUID Conversion Issues](./uuid-conversion): problems converting from name-based to UUID-based storage
- [Backend Errors](./backend-errors): errors related to SQL, H2, or file storage
- [Plugin Conflicts](./plugin-conflicts): another plugin is interfering with permissions

## Getting Help

If you're still experiencing issues after working through the relevant guide:

1. Run `/pex report` to generate an issue template URL
2. Run `/pex toggle debug` to enable debug logging
3. Check the server console for detailed debug output
4. Open an issue on [GitHub](https://github.com/rowan-smith/PermissionsExPlus/issues) with:
   - Server version and software
   - PermissionsExPlus version
   - Steps to reproduce
   - Console log excerpts (with debug mode enabled)
