# bestiary

Vanilla creature behavior tweaks ported from Sindusk's WyvernMods Bestiary
submodule. Each tweak is independently toggleable.

## Provenance

Ported from `mod.sin.wyvern.Bestiary` in upstream WyvernMods. Custom-creature
hooks (Wyvern dragons, Charger, Avenger, Spectral Drake, IceCat, Fire Crab,
Titans, RareSpawn map drops, Facebreyker club drop) are deferred to their
own community submods — they need template registration to land first.

This jar handles only the patches that work on stock vanilla creature templates.

## Toggles

| Key | What it does |
| --- | --- |
| `disableAfkTraining` | Block skill gain for AFK players whose target isn't the attacker, or whose weapon/armour combo can't deal real damage. |
| `fixSacrificingStrongCreatures` | Reject sacrifice on `creature.isUnique()` (vanilla dragons/drakes/troll-kings/cyclops/forest-giants/goblin-leaders). |
| `conditionWildCreatures` | 1-in-5 chance of a random `C_MOD_*` modifier on aggressive wild spawns; 1-in-50 of those becomes Champion. |
| `useCustomCreatureSizes` | Replace `CreatureStatus.getSizeMod()` with a version that honours `C_MOD_SIZESMALL` / `C_MOD_SIZEMINI` / `C_MOD_SIZETINY`. |
| `genesisEnchantedGrassNewborns` | Auto-Genesis any creature born on enchanted grass. |
| `preventLegendaryHitching` | Reject hitching unique creatures to vehicles. |
| `allowGhostArchery` | Let archery engage ghost-flagged creatures. |
| `logCreatureSpawns` | Log every `Creature.doNew` at INFO. Off by default. |
| `applyTemplateAdjustments` | At server-start, apply natural-armour / grazer / unique / Worg-as-mount / Cyclops-fighting / SoN combat-rating tweaks. |

## Deferred (need custom creature submods)

- Custom corpse models (WyvernBlack/Avenger/etc.)
- `setGhost` for spectral creatures (Avenger / SpiritTroll / Charger)
- `setNoCorpse` for Titan minions / IceCat / FireGiant
- `denyPathingOverride` for Charger (ghost wall-clipping)
- `allowGhostBreeding` (Charger-specific)
- `allowGhostCorpses` (Avenger / SpiritTroll / Charger)
- `useCustomCorpseSizes` (Avenger / Titan oversized corpses)
- `allowCustomCreatureBreedNames` (Wyvern breed-name pool)
- `useCustomCreatureSFX` (IceCat ice particles, Fire Crab flame attach, etc.)
- `modifyNewCreature` Titan/RareSpawn/Facebreyker hooks
- `isArcheryImmune` Titan-specific clauses (vanilla `isUnique()` already blocked)
- `isSacrificeImmune` Titan/Rare clauses (vanilla `isUnique()` already blocked)
- `isNotHitchable` custom creature clauses
- Custom corpse models / spell resistance / hitchable lists
