# gemaugmentation

Improvement-skill QL boost via the material's impalement bonus, plus a
raised QL ceiling so high-tier items can keep scaling. Direct port of
Sindusk's WyvernMods `GemAugmentation`.

## What it does

Each successful improve / polish / temper action runs through
`GemAugmentationPatches.setGemmedQuality(target, power, maxGain, modifier)`,
which multiplies the QL gain by `target.getMaterialImpBonus()`. Items
made of high-impBonus materials — addy, glimmer, seryll, etc. — improve
faster than the same item in iron.

In addition:

- Action power is preserved as a raw float (no dilution by the vanilla
  power-modifier path).
- The Gem Augmentation skill is locked from converting (forced
  `noChange=true` on its `MethodsReligion.listen → skillCheck` call).
- `DbItem.setQualityLevel`'s effective ceiling is raised from vanilla's
  9999.9f cap so items with multipliers can persist past the old cap.

## Bytecode patches

| Class | Method | Call site replaced | Purpose |
|-------|--------|--------------------|---------|
| `MethodsReligion` | `listen` | `skillCheck(...)` | force `noChange=true` so the skill doesn't get converted away |
| `MethodsItems` | `improveItem` / `polishItem` / `temper` | `setQualityLevel(...)` | route through `setGemmedQuality` |
| `MethodsItems` | `improveItem` / `polishItem` / `temper` | `setPower(...)` | keep raw float action power |
| `DbItem` | `setQualityLevel` | `Math.min(...)` | raise QL ceiling to 9999.9 |

## Configuration (`mods/gemaugmentation.properties`)

| Key | Default | Effect |
|-----|---------|--------|
| `enabled` | `true` | Master toggle. When false, no patches install. |

## Provenance

Ported from `ModSources/upstream/sindusk/wyvernmods/src/main/java/mod/sin/wyvern/GemAugmentation.java`.
