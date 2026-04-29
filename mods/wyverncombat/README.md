# wyverncombat

Combat-side residuals from Sindusk's WyvernMods `CombatChanges`. Four
independently-gated bytecode patches against vanilla `CombatHandler` and
`Wound`, plus a periodic poll that heals unique creatures.

## What it does

| Feature | Default | What it patches |
|---------|---------|-----------------|
| `combatRatingAdjustments` | `true` | Inserts an additive + multiplicative hook into `CombatHandler.getCombatRating` (right before the `getFlankingModifier` call). The additive hook gives Royal Executioner +2 CR vs non-players. The multiplicative hook scales pet CR by `ownerSoulDepth × 0.02` and applies a 0.75× CR penalty when the combatant is on a vehicle. Each sub-feature has its own toggle (`royalExecutionerBonus`, `petSoulDepthScaling`, `vehicleCombatRatingPenalty`). |
| `adjustCombatRatingSpellPower` | `true` | Halves the CR contribution of TrueHit / Excel / etc. by replacing the `getBonusForSpellEffect` call inside `CombatHandler.getCombatRating` with `proceed × 0.5`. DUSKombat owns its own hit/parry math, but vanilla CR is still consulted at several call sites — the nerf still bites. |
| `disableLegendaryRegeneration` | `true` | Short-circuits `Wound.poll`'s `modifySeverity`, `checkInfection`, `checkPoison` calls when `this.creature.isUnique()`, so uniques don't naturally heal. Pair with the periodic regen below. |
| `uniqueRegenerationIntervalSeconds` | `10` | Periodic 75-HP heal of one random wound on each living unique creature. Set to `0` to disable. |

## Patches dropped from upstream

- **`fixMagranonDamageStacking`** — DUSKombat's `DamageMethods` already
  replaces the entire damage-stacking code path; the `mildStack` flag is
  commented out there, and reapplying upstream's ×8/5 multiplier would
  fight with DUSKombat. Skip.
- **Life-transfer patches** (`doLifeTransfer`, `getLifeTransferModifier`,
  `getLifeTransferAmountModifier`) — already commented out upstream. Not
  ported.
- **Various debug instrumentation** in CombatChanges — also commented out
  upstream. Not ported.

## Configuration (`mods/wyverncombat.properties`)

| Key | Default |
|-----|---------|
| `enabled` | `true` |
| `combatRatingAdjustments` | `true` |
| `royalExecutionerBonus` | `true` |
| `petSoulDepthScaling` | `true` |
| `vehicleCombatRatingPenalty` | `true` |
| `adjustCombatRatingSpellPower` | `true` |
| `disableLegendaryRegeneration` | `true` |
| `uniqueRegenerationIntervalSeconds` | `10` |

## Provenance

Ported from `ModSources/upstream/sindusk/wyvernmods/src/main/java/mod/sin/wyvern/CombatChanges.java`.
