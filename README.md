# WurmModLoader Community Mods

A collection of community Wurm Unlimited mods ported from [Ago's original ModLoader](https://github.com/ago1024/WurmServerModLauncher) to the modern [Garward WurmModLoader](https://github.com/Garward/WurmModLoader).

Most of these started as Ago-era mods and have been updated to target the new event-based framework. Some have also been cleaned up, modernized, or extended in the process.

## Installing a mod

Every mod here ships as a drag-and-drop distribution. Grab its `dist/` folder and copy the contents into your server (or client) install:

```
<Wurm Unlimited Dedicated Server>/
├── mods/
│   ├── <modname>/
│   │   └── <modname>.jar
│   └── <modname>.properties
```

Client-side mods (under `client-mods/`) go into `WurmLauncher/mods/` the same way.

You need the Garward WurmModLoader installed on the server (and client, for client mods) — see the [main repo](https://github.com/Garward/WurmModLoader) for install instructions.

## Server mods (`mods/`)

| Mod | Purpose |
|-----|---------|
| announcer | Server-side announcements and chat broadcasts |
| armoury | Armour tuning and extensions |
| bagofholding | Expanded container capacity |
| betterdig | Quality-of-life digging improvements |
| betterfarm | Area farming with the new AreaAction framework |
| creatureagemod | Creature age tweaks |
| cropmod | Crop configuration and balance |
| DUSKombat | Combat overhaul (Sindusk's DUSKombat, ported) |
| harvesthelper | Harvest time / season utilities |
| inbreedwarning | Warns when breeding related animals |
| scriptrunner | Run scripts at server events |
| serverpacks | Serve custom texture/resource packs to connected clients |
| spellcraft | Spell framework extensions |
| spellmod | Tyoda's spell tweaks |

## Client mods (`client-mods/`)

| Mod | Purpose |
|-----|---------|
| action | Custom client-side action macros and keybinds |
| compass | On-screen compass HUD |
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
