# PermissionsExPlus Paper adapter

This module adapts PermissionsExPlus Core to Paper's Bukkit-compatible plugin environment. `PaperPlugin` owns the Core lifecycle, registers the player listener, and publishes the active API implementation.

Player data is loaded asynchronously on join, cached for runtime checks, and evicted on quit. Core uses the Paper/Bukkit scheduler for write-through persistence and expiry scans.

The adapter creates `config.yml` and `database.yml` in the plugin directory and stores local backend data under `data/`.
