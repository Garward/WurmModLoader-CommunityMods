# anticheat

Server-side anti-cheat patches lifted from Sindusk's WyvernMods anti-cheat
module. Each behavior is a toggle in `mods/anticheat.properties` — turn off
the ones you don't want.

> **Status:** skeleton release — the gradle module + mod.properties register
> cleanly and configuration is parsed, but no bytecode patches are wired up
> yet. Behaviors land in subsequent porting passes.

## What it does

### Cave-strip ore hiding
- **enableSpoofHiddenOre** — patch `Communicator.sendCaveStrip` so cave
  tiles that should not be visible (fully surrounded by walls, low
  prospecting) are sent to the client as plain rock. Defeats the classic
  "modified client" view-through-rock exploit. Visible tiles, cave exits,
  and surface tiles are unaffected.
- **prospectingVision** — above 20 prospecting, players can see veins
  through rock within `prospecting / 18` tiles. A soft replacement for the
  masking once skill is high enough.

### Steam ID mapping
- **mapSteamIds** — on login, record the player's Steam ID into a
  ModSupportDb table and warn in the server log if a single account is
  being accessed from multiple Steam IDs. Used by the bounty submod to
  attribute cross-account behavior.

## Soft dependencies

- **bounty** — `mapSteamIds` populates a name → SteamID map that the bounty
  submod consumes. Either order is fine; if bounty isn't loaded the map is
  recorded but unused.

## Provenance

Ported from the `## >> ANTI-CHEAT MODULE << ##` block of Sindusk's
WyvernMods (`mod.sin.wyvern.AntiCheat`). Toggle names preserved one-for-one
with upstream so existing operators can copy their `WyvernMods.properties`
values across without renaming. The upstream `enableAntiCheatModule` master
flag is omitted — under our split, the presence of the jar *is* the master.

The deprecated `isVisibleThroughTerrain` / `isVisibleToAntiCheat` helpers
(line-of-sight and ESP-counter logic) are not carried across — upstream
itself had them commented out and gated behind a `WyvernMods.espCounter`
toggle that no longer exists.
