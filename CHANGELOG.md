# Changelog

Tracked changes to the CommunityMods repo. Mod-specific changes also live in each mod's own headers/source — this file focuses on what was ported, what works, and what's in flux.

## [0.10.0] — 2026-04-23

Initial published release, aligned with [WurmModLoader 0.10.0](https://github.com/Garward/WurmModLoader/releases/tag/v0.10.0).

### Ported from Ago's ModLoader

Server mods:
- announcer, armoury, bagofholding, betterdig, betterfarm, creatureagemod, cropmod, DUSKombat,
  eventlister, harvesthelper, inbreedwarning, scriptrunner, serverpacks, spellcraft, spellmod, timerfix

Client mods (require the WurmModLoader client modloader):
- action, compass, wurmesp

All ports target the new event-based framework (`@SubscribeEvent`) rather than Ago's legacy listener interfaces.
A handful still use the legacy bridge where the modern event surface isn't fully covered yet — those
compile against `wurmmodloader-legacy` and will migrate as framework events land.

### Infrastructure

- Multi-project Gradle layout with `mods/<name>/` and `client-mods/<name>/` subprojects.
- Each mod produces a drag-and-drop `dist/` folder (`mods/<name>/<name>.jar` + `mods/<name>.properties`).
- Framework JARs consumed from `libs/` — auto-synced from sibling WurmModLoader / WurmModLoader-Client
  checkouts when building those from source.

### Known gaps

- Some upstream Ago mods still need porting: MightyMattock, SurfaceMiningFix, cavedwellingstweaks,
  and several others from the [ModSources](https://github.com/ago1024/WurmServerModLauncher) corpus.
- A few legacy-only mods (CustomCreatures, WyvernMods) are included in source form but not yet
  working against 0.10.0 — tracked as compatibility work.
