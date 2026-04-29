# qualityoflife

Quality-of-life adjustments around vehicles and casting that smooth out a
handful of vanilla Wurm Unlimited rough edges.

## What it does

Each behavior is independently toggleable in `mods/qualityoflife.properties`.

- **mineCaveToVehicle** — Cave-wall mining deposits the ore directly into
  the vehicle you are commanding. Tries BSBs first, then crates, then the
  cargo hold; falls back to dropping in front of you if everything is full.
- **mineSurfaceToVehicle** — Same, for surface mining.
- **chopLogsToVehicle** — Same, for chopping up felled logs.
- **mineGemsToVehicle** — Same, for tertiary materials (gems, source
  crystals, flint, salt).
- **statuetteAnyMaterial** — Allows casting using a statuette of any
  material, not just gold/silver. Wrong-deity statuettes are still
  rejected (e.g. a Vynora priest still cannot cast off a Fo statuette).
- **regenerateStaminaOnVehicleAnySlope** — Lets stamina regenerate while
  sitting stationary on a vehicle, regardless of slope. Vanilla disables
  regen on slopes >20.

## Soft dependencies

The four `*ToVehicle` toggles assume the player can take fatiguing actions
while commanding a vehicle. That's gated by `fatigueActionOverride` in the
**miscchanges** mod. Without miscchanges (or with that flag off), vanilla
blocks the action before this mod gets a chance to redirect it.

## Provenance

Ported from the `enableQualityOfLifeModule` block of Sindusk's WyvernMods
(`mod.sin.wyvern.QualityOfLife`). Property names preserved one-for-one so
existing operators can copy across the same values.
