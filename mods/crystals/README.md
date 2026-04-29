# crystals

Chaos crystals and enchanters crystals — random-table item modifiers.

## Item templates

| Resource id | Item | Notes |
|-------------|------|-------|
| `mod.chaoscrystal` | "chaos crystal" | Glowing crystal trophy. Used to roll a chaotic modifier on a repairable item. |
| `mod.item.crystal.enchanters` | "enchanters crystal" | Glowing crystal trophy. Used to roll a modifier on the enchant set of an enchanted item. |

Both are weight 250g, value 5000, banked, traded, image 462, model
`model.valrei.`.

## Actions

### Combine
- Activated crystal × same-template ground crystal. Skill: `MIND_LOGICAL`
  drives action time, `SOUL` drives the success roll, difficulty scales
  with combined QL + 20 × source rarity − caster's MIND knowledge.
- Same rarity required, both must be in the caster's hands. Pre-flight
  rejects mixed-template, self-target, rarity-mismatch, and a "would be
  too powerful" guard at rarity ≥ 3 with QL sum ≥ 100.
- Sum of QL > 100 → consume source, raise target rarity by 1, keep the
  overflow QL. Otherwise → consume source, raise target QL.
- Failure damages both crystals.

### Infuse (chaos)
- Activated chaos crystal × repairable item (no unfinished items).
- Source rarity must equal `target.rarity + 1` (one tier above) — anything
  else is rejected with a flavour message.
- `SOUL` skillcheck against `getInfusionDifficulty()` rolls a chaotic
  outcome:
  - `> 90` rarity copy from crystal to target
  - `> 60` material reroll (metal/wood pool) or color reroll
  - `> 30` random weight ±50–150%
  - `> 0` random QL
  - `> -20` damaged crystal, no other effect
  - `> -40` consume crystal, damage target
  - else: catastrophic — destroys both, except seryll target survives

### Infuse (enchanters)
- Activated enchanters crystal × any enchanted item.
- `SOUL` skillcheck repeated `rarity+1` times (best wins) against
  `getEnchantersInfusionDifficulty()`:
  - `> 90` add a new enchant from a 17-entry pool (BotD, Aura, etc.)
  - `> 75` increase all existing enchants up to +20%
  - `> 60` rotate one existing enchant to a new pool entry, preserving
    power
  - `> 35` shift each enchant ±30%
  - `> 0` remove a random enchant, increase the rest up to +20%
  - `> -30` reduce all enchants up to −20%
  - `> -60` remove a random enchant
  - else: wipe all enchants

## Configuration (`mods/crystals.properties`)

| Key | Default | Effect |
|-----|---------|--------|
| `enabled` | `true` | Master toggle. When false, no templates are registered and no actions are wired up. |

## Cross-submod coupling

These template ids are needed by:

- **caches** — drop them via `crystalCacheTemplateIds`. Once both ids are
  registered, set the property to repeat-weighted entries (e.g.
  `crystalCacheTemplateIds=<chaosId>,<chaosId>,<enchId>`).
- **soulstealing** — chaos crystal id is used in the optional eternal
  reservoir creation recipe. Set `chaosCrystalTemplateId=<chaosId>` and
  `registerCreationEntry=true`.
- **bounty** — `LootBounty.doRollingCrystalReward(...)` upstream drops both
  templates from rare-spawn / titan corpses; pending its port.

Look up the assigned template ids in the server log:

    [crystals] registered mod.chaoscrystal (id=NNN)
    [crystals] registered mod.item.crystal.enchanters (id=NNN)

## Provenance

Ported from `ModSources/upstream/sindusk/wyvernmods`:

- `mod/sin/wyvern/Crystals.java` → `CrystalsHelper`.
- `mod/sin/items/ChaosCrystal.java`, `EnchantersCrystal.java` →
  `CrystalsTemplates`.
- `mod/sin/actions/items/ChaosCrystalInfuseAction.java`,
  `EnchantersCrystalInfuseAction.java`, `CrystalCombineAction.java` →
  same class names, namespace rebased.
