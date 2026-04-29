# customtitles

Register additional `Titles.Title` enum entries from config and grant them to
named players on login. Ported from Sindusk's WyvernMods `PlayerTitles` module.

## What it does

### addCustomTitle

Reads property lines of the form

    addCustomTitle.<key> = id, maleName, femaleName, skillId, type

and registers each one through the framework's
`com.garward.wurmmodloader.api.titles.TitleRegistry`. The framework's
`TitleInjectionPatch` drains the queue once at boot and writes the entries
into the `Titles.Title` enum's static initializer (so `Titles.Title.getTitle(id)`
works for the rest of server lifecycle). Use ids ≥ 10000 to stay clear of
vanilla.

### awardTitle

Reads property lines of the form

    awardTitle.<key> = id, playerName1, playerName2, ...

and grants the title (any vanilla or custom id) to each named player on
`PlayerLoginEvent`. Names are matched case-sensitively against
`Player.getName()`.

## Provenance

Ported from `mod.sin.wyvern.PlayerTitles` (WyvernMods). Property names
match upstream so existing operator configs copy 1:1.

Upstream's `playerTitles` HashMap and `hasCustomTitle` / `getCustomTitle`
helpers (intended for donator/patron name suffixes) were dead in the
WyvernMods source — the map was never populated — so they're not ported.
The `TITAN_SLAYER` and `SPECTRAL` event-title constants moved to whichever
submod owns them (Bounty / Titan); they're plain awardable ids, no special
casing here.

The framework half (`TitleRegistry`, `TitleDefinition`, `TitleInjector`,
`TitleInjectionPatch`) supersedes the bdew_server_mod_tools `ModTitles`
dependency Sindusk used; mods can call `TitleRegistry.addTitle(...)`
directly from their own `preInit()` instead of going through this submod.
