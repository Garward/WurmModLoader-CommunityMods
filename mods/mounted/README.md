# mounted

Mount-speed scaling changes. Ported from Sindusk's WyvernMods
`MountedChanges` module. Each behavior is a toggle in
`mods/mounted.properties`.

## What it does

### newMountSpeedScaling

Replaces vanilla `Creature.getMountSpeedPercent` with a formula that gives
horseshoes (LEFT_FOOT / RIGHT_FOOT / LEFT_HAND / RIGHT_HAND) and saddles
(TORSO) proper weight for quality, rarity, and `getSpellSpeedBonus`. Also
factors trait movement bonus, hunger, damage, oakshell, and the creature's
movement-scheme modifier.

Implemented as a `MountSpeedPercentEvent` subscription (framework). When
the toggle is on, the subscriber calls `event.setPercent(...)` and the
framework's bytecode patch substitutes that value for vanilla's return.
Applies a small wear tick to each shoe (and the saddle if ridden) on every
recalculation, the same as upstream.

### updateMountSpeedOnDamage

Forces an immediate mount-speed recheck whenever the creature takes a
wound (vanilla otherwise repolls only every ~20 ticks, so wounds applied
to a moving mount visibly lag the speed update).

Mod-side bytecode patch on `Creature.setWounded` that calls
`forceMountSpeedChange()` before vanilla wound application. Too narrow for
a framework event.

## Provenance

Ported from `mod.sin.wyvern.MountedChanges` (WyvernMods). Toggle names
preserved one-for-one with upstream. Upstream's commented-out cloth /
leather / chain barding speed penalties are omitted (no canonical
template id mapping inside this submod yet).
