# soulstealing

Eternal Reservoirs that drain corpse-souls and feed nearby branded carnivores
plus lit forges.

## Player flow

1. **Steal a soul.** Activate a *sacrificial knife*, right-click an unbutchered
   corpse → "Soulsteal". A successful Stealing skillcheck against
   `40 - 0.4 * baseCombatRating` produces a *Soul* item whose name encodes
   the source creature, and whose QL is the skillcheck power. The corpse is
   destroyed regardless.
2. **Feed a reservoir.** Activate a Soul, right-click a deployed Eternal
   Reservoir → "Feed soul". A Soul Strength skillcheck against
   `25 - 0.2 * QL` raises the reservoir's `data1` (fuel) by the power
   amount. The Soul is destroyed regardless.
3. **Reservoirs tick.** Every `pollIntervalSeconds` (default 600), each
   reservoir scans tiles within radius `QL/10` for:
   - Branded carnivores with hunger > 10000 → consume 50 fuel, drop hunger by
     10000, broadcast "ethereal creature" message.
   - Lit forges/ovens with temperature < 20000 → consume 15 fuel, raise
     temperature by 10000, broadcast "is refueled" message.
4. **Check fuel.** Right-click a reservoir without anything activated →
   "Check fuel". Returns a flavour line scaling from `< 30` ("inactive") up
   to `>= 50000` ("absolutely flooded").

## Item templates

| Resource id | Item | Notes |
|-------------|------|-------|
| `mod.item.soul` | "soul" | Crystal trophy, weight 250g, value 5000, banked. Image 859, model `model.valrei.`. |
| `mod.item.eternal.reservoir` | "eternal reservoir" | Stone deployable, weight 200kg, value 10000, no-take, ground-only, has-data. Image 60, model `model.structure.rift.altar.1.`. |

## Configuration (`mods/soulstealing.properties`)

| Key | Default | Effect |
|-----|---------|--------|
| `enabled` | `true` | Master toggle. |
| `pollIntervalSeconds` | `600` | Reservoir tick cadence. |
| `registerCreationEntry` | `false` | If true, registers the upstream pottery recipe (dirt-pile + bricks + chaos-crystal + heart). |
| `chaosCrystalTemplateId` | `-1` | Template id of the chaos-crystal item used by the recipe. Required when `registerCreationEntry=true`. The crystals submod ships this template; until then leave at `-1`. |

## Provenance

Ported from `ModSources/upstream/sindusk/wyvernmods`:

- `mod/sin/wyvern/Soulstealing.java` → `SoulstealingPoll`, `SoulstealingMod`.
- `mod/sin/items/Soul.java`, `EternalReservoir.java` → `SoulstealingTemplates`.
- `mod/sin/actions/items/SoulstealAction.java` → `SoulstealAction`.
- `mod/sin/actions/items/EternalReservoirCheckFuelAction.java` →
  `EternalReservoirCheckFuelAction`.
- `mod/sin/actions/items/EternalReservoirRefuelAction.java` →
  `EternalReservoirRefuelAction`.

The upstream module wired its poll into a manual cron inside
`WyvernMods.poll()`; this port subscribes to `ServerPollEvent` with a
local timestamp throttle.
