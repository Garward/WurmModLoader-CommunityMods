# mastercraft

Skill-mastery and channeling-mastery rewards. Three independent knobs that
make late-game progression *feel* like progression.

## Skill check difficulty bonuses

Hooks `SkillAdvanceEvent` and shaves difficulty off every tick:

| Toggle | Effect |
|--------|--------|
| `affinityDifficultyBonus` | -1 difficulty per affinity tick on the skill |
| `legendDifficultyBonus`   | Knowledge > 99 → up to -2 difficulty (linear, max at 100.0) |
| `masterDifficultyBonus`   | Knowledge > 90 → up to -2 difficulty (linear, max at 100.0) |
| `itemRarityDifficultyBonus`   | -1 / -2 / -3 difficulty for rare / supreme / fantastic tools |
| `legendItemDifficultyBonus`   | Item QL > 99 → up to -1 difficulty |
| `masterItemDifficultyBonus`   | Item QL > 90 → up to -1 difficulty |

All six stack — a 100-skill priest swinging a fantastic 100QL tool gets
the maximum reduction on every roll.

## Channeling power boost

Hooks `SpellPowerEvent` (fired immediately after vanilla `Spell.trimPower`).
The channeling skill of the caster adds:

- `2 × affinity` flat bonus per affinity tick.
- A skill-knowledge biased random roll (min of two min-of-two rolls,
  scaled by knowledge) — heavy bias toward 0 at low skill, scales up to
  ~70-80 at maxed channeling.

Toggle: `empoweredChannelers`.

## Channeling favor reduction

Hooks `SpellFavorCostEvent`. Reduces favor consumed by:

- `2%` per affinity tick.
- Up to `~10%` when channeling > 90.
- Another up to `~10%` when channeling > 99.

Toggle: `channelSkillFavorReduction`.

## Provenance

Ported from `mods/WyvernMods/src/main/java/mod/sin/wyvern/Mastercraft.java`.
Upstream installed three ad-hoc bytecode patches against `Skill.checkAdvance`
and `Spell.run`; we use the existing framework events
(`SkillAdvanceEvent`, `SpellPowerEvent`, `SpellFavorCostEvent`) so this
submod ships as pure event handlers — no patches.
