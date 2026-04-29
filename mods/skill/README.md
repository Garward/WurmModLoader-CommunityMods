# skill

Skill-gain curve, skill renames, difficulty/tick-time overrides, and an
optional Preaching dependency rewrite. Ported from Sindusk's WyvernMods skill
module. Each behavior is a toggle in `mods/skill.properties` — turn off the
ones you don't want.

## What it does

### Hybrid skill gain
- **enableHybridSkillGain** — replaces the vanilla flat skill-gain multiplier
  with a Wurm-Online-style curve where ticks closer to zero give more skill
  than easy ticks. Subscribes to the framework's `SkillGainMultiplierEvent`
  (no bytecode patching in mod-space).

  Tunables (all `> 0`, must be sane against each other):
  - **hybridNegativeDecayRate** (default `5`) — sharpness of the decline past
    zero going negative.
  - **hybridPositiveDecayRate** (default `3`) — sharpness of the decline past
    zero going positive.
  - **hybridValueAtZero** (default `3.74`) — multiplier at `power = 0`. Must
    exceed `hybridValueAtOneHundred`.
  - **hybridValueAtOneHundred** (default `0.9`) — multiplier at `power = 100`.

  Disable to revert to vanilla skill gain. The curve graph upstream ships
  with: <https://i.imgur.com/9ykw5dJ.png>

### Skill renames
- **skillName-#** — `skillName-#:<skill>,<newName>`. `<skill>` is either a
  numeric ID (e.g. `1008`) or a case-insensitive skill name (e.g. `mining`,
  `"Lock Picking"`). Carries to the in-game skill list.

  Default config renames Preaching → "Gem augmentation" and Stealing →
  "Soulstealing" so the GemAug + Soulstealing submods read naturally. Comment
  the lines out if you don't ship those mods.

### Skill difficulty overrides
- **skillDifficulty-#** — `skillDifficulty-#:<skill>,<difficulty>`. Most
  vanilla skills sit at `4000`. Lower = faster gain (`2000` doubles, `8000`
  halves). Default config tunes Mining (8000 → 3000), Lock Picking (2000 →
  700), and Meditating (2000 → 300).

### Skill tick-time overrides
- **skillTickTime-#** — `skillTickTime-#:<skill>,<milliseconds>`. The minimum
  interval between consecutive ticks of a given skill. Default config sets
  Stealing to `0` (so the Soulstealing submod isn't gated by the vanilla
  10-minute Stealing timer) and Meditating to `3600000` (1 hour).

### Move Preaching under Masonry
- **changePreachingLocation** — re-parents the Preaching skill so it depends
  on Masonry instead of Religion. Used by the GemAug submod which surfaces
  Preaching as "Gem augmentation." Recommended off if you aren't shipping
  GemAug.

## Provenance

Ported from the `## >> SKILL MODULE << ##` block of Sindusk's WyvernMods
(`mod.sin.wyvern.SkillChanges`). Toggle names preserved one-for-one with
upstream so existing operators can copy their `WyvernMods.properties` values
across without renaming. The upstream `enableSkillModule` master flag is
omitted — under our split, the presence of the jar *is* the master.

The hybrid-skill-gain curve is wired through the framework's
`SkillGainMultiplierEvent` rather than a per-mod `Skill.doSkillGainNew`
bytecode patch — the patch lives in the framework once and any number of mods
can compose multipliers on top.
