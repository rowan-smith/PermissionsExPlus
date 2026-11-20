# Legacy plugin fixtures

Real, unmodified plugins of the PermissionsEx 1.x era, kept to check that plugins built
against the original PermissionsEx still work against the API adapter:

- `LegacyPluginLinkageTest` checks every PermissionsEx method, constructor and field each
  jar references links against the adapter.
- CI's smoke runs with `legacy_plugins: true` load them on real servers next to Vault and
  the LegacyPexProbe (`scripts/legacy-plugins`). ChatEx needs Minecraft 1.18+, so it only
  runs on the Java 21 servers.

| Jar | Version | Uses PermissionsEx through |
| --- | --- | --- |
| ChatEx.jar | 3.3.0 | Vault |
| ColorMe.jar | 3.8.1 | PermissionsEx API (`PermissionsEx.getUser`) and Vault |
| FactionsChat.jar | 1.4 | PermissionsEx API |
| PermissionsExAddon.jar | 1.3 | PermissionsEx API (`PermissionsEx.getUser`) |
| PexManager.jar | 1.5 for 1.12.2 | PermissionsEx API (`PermissionsEx.getUser`, `setGroups`) |
| PrefixedPlayerlist.jar | 1.1.9 | PermissionsEx API |
| WordRankUp.jar | 1.2.2 | PermissionsEx API (`setGroups(List)`, from PermissionsEx 1.21) and Vault |

They are test fixtures only and are never packaged into a plugin jar.
