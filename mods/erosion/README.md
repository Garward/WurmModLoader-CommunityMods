# erosion

Periodic background terrain smoothing. On a configurable interval, picks a
random surface tile and smooths a 20×20 area around it if it qualifies.
PvE-only — disables itself when `Servers.localServer.PVPSERVER` is set.

## Provenance

Ported from Sindusk's WyvernMods `enableErosionModule` toggle + the poll
that drives `SmoothTerrainAction.onServerPoll()` (originally fired from
the WyvernMods server-poll dispatcher every 30s).

The smoothing routine is duplicated from `mods/extraactions`'
`SmoothTerrainPerformer` so the two jars stay independent. Both share the
same eligibility rules: smoothable tile types only, never inside deeds /
perimeters / structures, only bumps and slants > 10 height units.
