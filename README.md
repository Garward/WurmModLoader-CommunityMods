# WurmModLoader Community Mods

A collection of community Wurm Unlimited mods ported from [Ago's original ModLoader](https://github.com/ago1024/WurmServerModLauncher) to the modern [Garward WurmModLoader](https://github.com/Garward/WurmModLoader).

Most started as Ago-era mods and have been updated to target the new event-based framework. About 30 of the server mods come from a single source — Sindusk's **WyvernMods** kitchen-sink — which has been sliced into independent, focused submods so operators can mix and match instead of opting into the whole bundle. A few originated from other authors (bdew, joedobo27, Tyoda, WalkerInTheVoid) or are framework-native.

## Installing a mod

Every mod ships as a drag-and-drop distribution. Drop its folder into your server's `mods/` directory in the **self-contained subfolder** layout:

```
<Wurm Unlimited Dedicated Server>/
└── mods/
    ├── enabled.json                ← optional master toggle
    └── <modname>/
        ├── mod.properties          ← REQUIRED (descriptor: classname, classpath)
        ├── mod.config              ← optional mod-specific settings
        ├── icons/                  ← optional PNG icons (auto-scanned & packed)
        │   └── *.png
        └── <modname>.jar           ← REQUIRED (unversioned filename)
```

Only `mod.properties` + `<modname>.jar` are required; `mod.config` and `icons/` are optional. Most mods won't ship either.

Client-side mods (under `client-mods/`) go into `WurmLauncher/mods/` the same way.

The Garward WurmModLoader must be installed on the server (and client, for client mods) — see the [main repo](https://github.com/Garward/WurmModLoader) for install instructions.

### Enabling / disabling mods

`mods/enabled.json` is the canonical master toggle. Missing entries default to **enabled**, so you only need to list mods you want to turn off:

```json
{
  "experimentalmod": false,
  "anothermod": false
}
```

## Server mods (`mods/`)

### WyvernMods slices

Sindusk's WyvernMods has been split into independent features. Mix and match — there is no master switch.

| Mod | Purpose |
|-----|---------|
| achievementchanges | Renames internal-looking vanilla achievement strings |
| anticheat | Server-side anti-cheat patches from WyvernMods |
| arena | ~35 PvP-only patches, gated on `PVPSERVER`; dormant on PvE |
| armoury | Armour tuning and extensions (Sindusk's Armoury) |
| bestiary | Vanilla creature behavior tweaks (corpse / loyalty / sfx) |
| bounty | Coin-on-kill + corpse-loot framework with public `BountyRegistry` SPI |
| caches | 11 tiered loot caches + open action |
| crystals | Chaos + enchanters crystals + 3 actions |
| customdeity | Config-driven deity flag / affinity / template overrides |
| customtitles | Register additional `Titles.Title` enum entries from config |
| DUSKombat | Combat overhaul (Sindusk's DUSKombat) |
| economy | NPC trader / economic balance changes |
| erosion | Periodic background terrain smoothing (PvE-only) |
| extraactions | Generic + GM actions from WyvernMods |
| gemaugmentation | impBonus QL multiplier on imp/polish/temper; raised QL ceiling |
| keyevent | World-event ritual + 4 tomes (key fragment, enchant/eternal/affinity orbs) |
| mastercraft | Skill / channeling mastery rewards |
| meditation | Meditation-path balance changes (Love/Hate/Insanity/Knowledge/Power) |
| miscchanges | Vanilla template tweaks (item-tweak submod folded in here) |
| mission | Epic-mission tweaks + GM Add/Remove body-menu actions |
| mounted | Mount-speed scaling changes |
| qualityoflife | QoL adjustments around vehicles and casting |
| rarespawn | Reaper / SpectralDrake / WyvernBlue world boss spawns |
| skill | Skill-gain curve, renames, difficulty / tick-time overrides |
| soulstealing | Eternal Reservoir + Soul + 3 actions |
| supplydepot | World-boss supply depot + capture action + reward bundle |
| teleport | Custom teleport actions and arena-server transfer landing |
| titan | Lilith / Ifrit raid bosses with scripted boss fight |
| treasurechest | Tiered chest loot |
| wyverncombat | CombatChanges residuals (CR adjustments + spell-effect nerf + legendary regen disable) |

### Other ports & native mods

| Mod | Purpose |
|-----|---------|
| announcer | Server-side announcements and chat broadcasts |
| bagofholding | Expanded container capacity (bdew) |
| betterdig | Quality-of-life digging improvements |
| betterfarm | Area farming via the framework's AreaAction system |
| creatureagemod | Creature age tweaks |
| cropmod | Crop configuration and balance |
| harvesthelper | Harvest time / season utilities |
| inbreedwarning | Warns when breeding related animals |
| powerscaling | Power-scaling system (Garward) |
| scriptrunner | Run hot-reloadable Nashorn JS scripts at server events |
| spellcraft | Spell framework extensions |
| spellmod | Tyoda's spell tweaks |
| testmod | Internal test scaffolding (skip in production) |

> **`serverpacks`:** the standalone `mods/serverpacks/` package is now a deprecated **compat shim** — server-pack hosting was promoted into the framework. The folder may be safely deleted on installs running current WurmModLoader.

## Client mods (`client-mods/`)

| Mod | Purpose |
|-----|---------|
| action | Custom client-side action macros and keybinds |
| automine | Auto-mining helper |
| compass | On-screen compass HUD |
| tooltips | Hover-name / hover-description tooltips for tiles, walls, and creatures |
| wurmesp | Wireframe overlay for entities |

## Building from source

Requires JDK 17 and a local Wurm Unlimited install.

```bash
# point these at your install(s) in ~/.gradle/gradle.properties or via env:
#   wurmServerDir=/path/to/Wurm Unlimited Dedicated Server
#   wurmClientDir=/path/to/Wurm Unlimited/WurmLauncher

./gradlew build
```

Each mod's final distribution lands in `mods/<name>/dist/` (or `client-mods/<name>/dist/`).

## License

Individual mods retain their original authors' licenses where applicable. Ports and repo-level tooling are released under the same terms as the main WurmModLoader project (MIT).

## Credits

Original mod authors include Ago, bdew, joedobo27, Sindusk, Tyoda, WalkerInTheVoid, and others — see each mod's source headers for specific attribution. This repo exists to keep their work running on modern WurmModLoader.
